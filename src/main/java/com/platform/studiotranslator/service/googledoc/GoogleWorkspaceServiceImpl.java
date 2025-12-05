package com.platform.studiotranslator.service.googledoc;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
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
                            DriveScopes.DRIVE_FILE // Manage files created by this app
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

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize Google Workspace Services", e);
        }
    }

    @Override
    public String createDocument(String title) {
        try {
            Document doc = new Document().setTitle(title);
            Document result = docsService.documents().create(doc).execute();
            log.info("Created Google Doc: {} ({})", title, result.getDocumentId());
            return result.getDocumentId();
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

    /**
     * Helper to turn Google's JSON structure into basic HTML.
     * Google returns a list of "StructuralElements" (Paragraphs, Tables, etc.)
     */
    private String convertGoogleDocToHtml(Document doc) {
        StringBuilder html = new StringBuilder();
        List<StructuralElement> elements = doc.getBody().getContent();

        for (StructuralElement element : elements) {
            if (element.getParagraph() != null) {
                renderParagraph(element.getParagraph(), html);
            } else if (element.getTable() != null) {
                // For MVP, we might skip tables or render them simply
                html.append("<p><i>[Table Content - Not Supported Yet]</i></p>");
            }
        }
        return html.toString();
    }

    private void renderParagraph(Paragraph paragraph, StringBuilder html) {
        ParagraphStyle style = paragraph.getParagraphStyle();
        String tag = "p";

        // Handle Headings (Heading_1 -> h1)
        if (style != null && style.getNamedStyleType() != null) {
            String type = style.getNamedStyleType();
            if (type.startsWith("HEADING_")) {
                tag = "h" + type.substring(8); // HEADING_1 -> h1
            }
        }

        html.append("<").append(tag).append(">");

        // Paragraph contains a list of "elements" (Text Runs)
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

        // Apply formatting
        if (Boolean.TRUE.equals(style.getBold())) html.append("<b>");
        if (Boolean.TRUE.equals(style.getItalic())) html.append("<i>");
        if (Boolean.TRUE.equals(style.getUnderline())) html.append("<u>");

        // Sanitize content (basic)
        content = content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Handle newlines
        content = content.replace("\n", ""); // Paragraph tag handles the block, remove internal newlines

        html.append(content);

        if (Boolean.TRUE.equals(style.getUnderline())) html.append("</u>");
        if (Boolean.TRUE.equals(style.getItalic())) html.append("</i>");
        if (Boolean.TRUE.equals(style.getBold())) html.append("</b>");
    }
}
