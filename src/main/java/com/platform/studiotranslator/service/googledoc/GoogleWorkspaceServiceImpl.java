package com.platform.studiotranslator.service.googledoc;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleWorkspaceServiceImpl implements GoogleWorkspaceService {

    private static final String APPLICATION_NAME = "StudioTranslator/1.0";

    // The ID of the folder you created in your personal Drive
    private static final String TARGET_FOLDER_ID = "1BzKp8CHJ7OhcFjU_7ZvFCYFtnl0baOcr";

    @Value("${application.google.credentials-path:credentials.json}")
    private String credentialsPath;

    private Docs docsService;
    private Drive driveService;

    @PostConstruct
    public void init() {
        try {
            // Load the Service Account Credentials
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                            new ClassPathResource(credentialsPath).getInputStream())
                    .createScoped(List.of(
                            DocsScopes.DOCUMENTS,  // Read/Write Docs
                            DriveScopes.DRIVE // Full Drive access required for file creation
                    ));

            var requestFactory = new HttpCredentialsAdapter(credentials);
            var jsonFactory = GsonFactory.getDefaultInstance();
            var transport = GoogleNetHttpTransport.newTrustedTransport();

            this.docsService = new Docs.Builder(transport, jsonFactory, requestFactory)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            this.driveService = new Drive.Builder(transport, jsonFactory, requestFactory)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Run cleanup once if your storage is full, then comment it out
             cleanupStorage();

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize Google Workspace Services", e);
        }
    }

    /**
     * UTILITY: Deletes ALL files created by this Service Account.
     * WARNING: Use only for development/cleanup to fix "storageQuotaExceeded".
     */
    public void cleanupStorage() {
        try {
            log.warn("STARTING STORAGE CLEANUP...");
            String pageToken = null;
            do {
                FileList result = driveService.files().list()
                        .setQ("'me' in owners and trashed = false")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();

                for (com.google.api.services.drive.model.File file : result.getFiles()) {
                    try {
                        driveService.files().delete(file.getId()).execute();
                        log.info("Deleted file: " + file.getName() + " (" + file.getId() + ")");
                    } catch (Exception e) {
                        log.error("Could not delete: " + file.getId());
                    }
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);

            driveService.files().emptyTrash().execute();
            log.info("STORAGE CLEANUP COMPLETE. Trash emptied.");

        } catch (IOException e) {
            log.error("Cleanup failed", e);
        }
    }

    @Override
    public String createDocument(String title) {
        try {
            // Use Drive API to create the file inside your specific Folder
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(title);
            fileMetadata.setMimeType("application/vnd.google-apps.document");
            // Set the parent folder
            fileMetadata.setParents(Collections.singletonList(TARGET_FOLDER_ID));

            com.google.api.services.drive.model.File file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .setSupportsAllDrives(true) // Ensure it works if you move to a Shared Drive later
                    .execute();

            log.info("Created Google Doc in folder {}: {} ({})", TARGET_FOLDER_ID, title, file.getId());
            return file.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Google Doc", e);
        }
    }

    @Override
    public void shareDocument(String docId, String email) {
        try {
            Permission permission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress(email);

            driveService.permissions().create(docId, permission)
                    .setSendNotificationEmail(false)
                    .execute();

            log.info("Silently shared Doc {} with {}", docId, email);
        } catch (IOException e) {
            throw new RuntimeException("Failed to share Google Doc", e);
        }
    }

    @Override
    public String getDocumentContent(String docId) {
        try {
            Document doc = docsService.documents().get(docId).execute();
            return convertGoogleDocToHtml(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Google Doc content", e);
        }
    }

    private String convertGoogleDocToHtml(Document doc) {
        StringBuilder html = new StringBuilder();
        List<StructuralElement> elements = doc.getBody().getContent();

        for (StructuralElement element : elements) {
            if (element.getParagraph() != null) {
                renderParagraph(element.getParagraph(), html);
            } else if (element.getTable() != null) {
                html.append("<p><i>[Table Content - Not Supported Yet]</i></p>");
            }
        }
        return html.toString();
    }

    private void renderParagraph(Paragraph paragraph, StringBuilder html) {
        ParagraphStyle style = paragraph.getParagraphStyle();
        String tag = "p";

        if (style != null && style.getNamedStyleType() != null) {
            String type = style.getNamedStyleType();
            if (type.startsWith("HEADING_")) {
                tag = "h" + type.substring(8);
            }
        }

        html.append("<").append(tag).append(">");

        for (ParagraphElement element : paragraph.getElements()) {
            if (element.getTextRun() != null) {
                renderTextRun(element.getTextRun(), html);
            }
        }

        html.append("</").append(tag).append(">");
    }

    private void renderTextRun(TextRun textRun, StringBuilder html) {
        String content = textRun.getContent();
        TextStyle style = textRun.getTextStyle();

        if (Boolean.TRUE.equals(style.getBold())) html.append("<b>");
        if (Boolean.TRUE.equals(style.getItalic())) html.append("<i>");
        if (Boolean.TRUE.equals(style.getUnderline())) html.append("<u>");

        content = content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        content = content.replace("\n", "");

        html.append(content);

        if (Boolean.TRUE.equals(style.getUnderline())) html.append("</u>");
        if (Boolean.TRUE.equals(style.getItalic())) html.append("</i>");
        if (Boolean.TRUE.equals(style.getBold())) html.append("</b>");
    }
}