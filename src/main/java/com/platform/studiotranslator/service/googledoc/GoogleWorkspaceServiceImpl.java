package com.platform.studiotranslator.service.googledoc;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleWorkspaceServiceImpl implements GoogleWorkspaceService {

    private final Drive driveService;

    private static final String PARENT_FOLDER_ID = "1BzKp8CHJ7OhcFjU_7ZvFCYFtnl0baOcr";

    @Override
    public String createDocument(String title) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(title);
            fileMetadata.setMimeType("application/vnd.google-apps.document");
            fileMetadata.setParents(Collections.singletonList(PARENT_FOLDER_ID));

            File file = driveService.files().create(fileMetadata)
                    .setFields("id") // Only request the ID back to save bandwidth
                    .execute();

            return file.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Google Doc", e);
        }
    }

    @Override
    public void shareDocument(String docId, String email) {
        try {
            // "user", "group", "domain", or "anyone"
            Permission permission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress(email);

            driveService.permissions().create(docId, permission)
                    .setSendNotificationEmail(true) // Optional: sends an email to the user
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to share document with " + email, e);
        }
    }

    @Override
    public String getDocumentContent(String docId) {
        try {
            // We use the Drive API to 'export' the proprietary Google Doc format
            // into standard HTML or Plain Text.
            // MIME type for HTML export: "text/html"
            // MIME type for Plain Text: "text/plain"
            String exportMimeType = "text/html";

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            driveService.files().export(docId, exportMimeType)
                    .executeMediaAndDownloadTo(outputStream);

            return outputStream.toString(StandardCharsets.UTF_8);

        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new RuntimeException("Document not found: " + docId);
            }
            throw new RuntimeException("Google Drive API error", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch document content", e);
        }
    }
}