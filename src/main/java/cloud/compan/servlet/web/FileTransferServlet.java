package cloud.compan.servlet.web;

import cloud.compan.servlet.model.transfer.FileChunk;
import cloud.compan.servlet.model.transfer.UploadSession;
import cloud.compan.servlet.service.transfer.ChunkService;
import cloud.compan.servlet.utils.FileTransferUtils;
import cloud.compan.servlet.utils.JsonUtils;
import cloud.compan.servlet.utils.ServiceLocate;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebServlet("/file/transfer")
public class FileTransferServlet extends HttpServlet {

    private ChunkService chunkService;
    private JsonUtils jsonUtils;

    @Override
    public void init() {
        // 使用ServiceLocator获取服务实例
        this.chunkService = ServiceLocate.getBean(ChunkService.class);
        this.jsonUtils = ServiceLocate.getBean(JsonUtils.class);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        if (action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing action parameter");
            return;
        }

        try {
            switch (action) {
                case "init": handleInit(req, resp); break;
                case "upload": handleUpload(req, resp); break;
                case "complete": handleComplete(req, resp); break;
                case "resume": handleResume(req, resp); break;
                default: resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action: " + action);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private void handleInit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fileName = req.getParameter("fileName");
        if (fileName == null || fileName.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing fileName parameter");
            return;
        }

        long fileSize;
        try {
            fileSize = Long.parseLong(req.getParameter("fileSize"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fileSize parameter");
            return;
        }

        UploadSession session = chunkService.initUploadSession(fileName, fileSize);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("sessionId", session.getSessionId());
        responseData.put("chunkSize", FileTransferUtils.getChunkSize());

        sendJsonResponse(resp, responseData);
    }

    private void handleUpload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getParameter("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing sessionId parameter");
            return;
        }

        int chunkIndex;
        try {
            chunkIndex = Integer.parseInt(req.getParameter("index"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid index parameter");
            return;
        }

        String checksum = req.getParameter("checksum");
        if (checksum == null || checksum.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing checksum parameter");
            return;
        }

        FileChunk chunk = new FileChunk();
        chunk.setSessionId(sessionId);
        chunk.setIndex(chunkIndex);
        chunk.setChecksum(checksum);
        chunk.setData(req.getInputStream());

        chunkService.saveChunk(chunk);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleComplete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getParameter("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing sessionId parameter");
            return;
        }

        String fileId = chunkService.mergeChunks(sessionId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("fileId", fileId);

        sendJsonResponse(resp, responseData);
    }

    private void handleResume(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getParameter("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing sessionId parameter");
            return;
        }

        Set<Integer> missingChunks = chunkService.getMissingChunks(sessionId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("missingChunks", new ArrayList<>(missingChunks));
        responseData.put("chunkSize", FileTransferUtils.getChunkSize());

        sendJsonResponse(resp, responseData);
    }

    /**
     * 发送JSON响应
     * @param resp HttpServletResponse对象
     * @param data 要发送的数据对象
     * @throws IOException 如果写入响应时出错
     */
    private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = resp.getWriter()) {
            String json = jsonUtils.toJson(data);
            writer.write(json);
            writer.flush();
        }
    }
}