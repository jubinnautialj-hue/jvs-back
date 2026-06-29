package cn.bctools.document.auth.util;

import cn.bctools.document.auth.dto.DocumentAuthDto;
import cn.bctools.document.auth.dto.LibraryAuthDto;
import lombok.extern.slf4j.Slf4j;

/**
 * @author admin
 */
@Slf4j
public class AuthSystemTool {
    /**
     * 文库权限标识
     */
    private static final String LIBRARY_AUTH = "libraryAuth";
    /**
     * 文档权限
     */
    private static final String DOCUMENT_AUTH = "documentAuth";

    public static LibraryAuthDto getLibraryAuth() {
        return SystemThreadLocal.get(LIBRARY_AUTH);
    }

    public static void setLibraryAuth(LibraryAuthDto libraryAuth) {
        SystemThreadLocal.set(LIBRARY_AUTH, libraryAuth);
    }

    public static DocumentAuthDto getDocumentAuth() {
        return SystemThreadLocal.get(DOCUMENT_AUTH);
    }

    public static void setDocumentAuth(DocumentAuthDto documentAuth) {
        SystemThreadLocal.set(DOCUMENT_AUTH, documentAuth);
    }

    public static void removeAll() {
        SystemThreadLocal.removeAll();
    }

    public static void removeLibraryAuth() {
        SystemThreadLocal.remove(LIBRARY_AUTH);
    }

    public static void removeDocumentAuth() {
        SystemThreadLocal.remove(DOCUMENT_AUTH);
    }

}
