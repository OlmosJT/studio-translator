package com.platform.studiotranslator.service.googledoc;

public interface GoogleWorkspaceService {
    /**
     * Creates a blank Google Doc and returns the Doc ID.
     */
    String createDocument(String title);

    /**
     * Shares the document with the specific email (Editor access).
     */
    void shareDocument(String docId, String email);

    /**
     * Fetches the content.
     * NOTE: Google APIs return JSON structure. You'll need a converter to HTML/Markdown.
     * For now, this returns the raw text or converted HTML.
     */
    String getDocumentContent(String docId);
}
