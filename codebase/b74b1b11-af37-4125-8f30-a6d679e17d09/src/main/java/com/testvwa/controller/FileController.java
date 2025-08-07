package com.testvwa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/file")
public class FileController {

    private static final Logger logger = Logger.getLogger(FileController.class);
    private static final String UPLOAD_DIR = "C:\\uploads\\";

    @RequestMapping("/upload")
    public String uploadForm() {
        return "file-upload";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadFile(@RequestParam("file") MultipartFile file,
                            @RequestParam(required = false) String description,
                            Model model) {
        
        try {
            if (!file.isEmpty()) {
                // VULNERABILITY: No file type validation
                // VULNERABILITY: No file size limits
                // VULNERABILITY: Directory traversal possible
                
                String originalFilename = file.getOriginalFilename();
                logger.info("Uploading file: " + originalFilename);
                
                // VULNERABILITY: Using user-provided filename directly
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                File destFile = new File(uploadDir, originalFilename);
                file.transferTo(destFile);
                
                // VULNERABILITY: Exposing full file paths
                model.addAttribute("success", "File uploaded successfully to: " + destFile.getAbsolutePath());
                model.addAttribute("filename", originalFilename);
                
            } else {
                model.addAttribute("error", "Please select a file to upload");
            }
        } catch (Exception e) {
            // VULNERABILITY: Exposing detailed error information
            model.addAttribute("error", "Upload failed: " + e.getMessage());
            logger.error("File upload error", e);
        }
        
        return "file-upload";
    }

    @RequestMapping("/download")
    public void downloadFile(@RequestParam String filename,
                           HttpServletResponse response) {
        
        try {
            // VULNERABILITY: Directory traversal - no path validation
            File file = new File(UPLOAD_DIR + filename);
            
            if (file.exists()) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                
                FileInputStream fis = new FileInputStream(file);
                OutputStream os = response.getOutputStream();
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                
                fis.close();
                os.close();
            } else {
                response.sendError(404, "File not found: " + filename);
            }
            
        } catch (Exception e) {
            logger.error("Download error", e);
        }
    }

    @RequestMapping("/view")
    public void viewFile(@RequestParam String file,
                        HttpServletResponse response) throws IOException {
        
        // VULNERABILITY: Local File Inclusion
        try {
            File targetFile = new File(file); // Direct file path from user
            
            if (targetFile.exists()) {
                response.setContentType("text/plain");
                
                BufferedReader reader = new BufferedReader(new FileReader(targetFile));
                PrintWriter writer = response.getWriter();
                
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                }
                
                reader.close();
                writer.close();
            } else {
                response.sendError(404, "File not found: " + file);
            }
            
        } catch (Exception e) {
            response.getWriter().println("Error reading file: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/xml", method = RequestMethod.POST)
    @ResponseBody
    public String processXML(@RequestBody String xmlContent) {
        
        try {
            // VULNERABILITY: XXE (XML External Entity) injection
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // VULNERABILITY: External entities not disabled
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
            
            return "XML processed successfully. Root element: " + doc.getDocumentElement().getNodeName();
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return "XML processing error: " + e.getMessage();
        }
    }

    @RequestMapping(value = "/deserialize", method = RequestMethod.POST)
    @ResponseBody
    public String deserializeJson(@RequestBody String jsonContent) {
        
        try {
            // VULNERABILITY: Insecure deserialization
            ObjectMapper mapper = new ObjectMapper();
            // VULNERABILITY: Default typing enabled (if configured elsewhere)
            Map<String, Object> data = mapper.readValue(jsonContent, Map.class);
            
            return "Deserialization successful. Keys: " + data.keySet().toString();
            
        } catch (Exception e) {
            return "Deserialization error: " + e.getMessage();
        }
    }

    @RequestMapping("/include")
    public String includeFile(@RequestParam String page, Model model) {
        // VULNERABILITY: Local File Inclusion via view name
        logger.info("Including page: " + page);
        
        try {
            // VULNERABILITY: No validation of page parameter
            return page; // Direct return of user input as view name
        } catch (Exception e) {
            model.addAttribute("error", "Include error: " + e.getMessage());
            return "error";
        }
    }

    @RequestMapping("/remote")
    @ResponseBody
    public String fetchRemoteFile(@RequestParam String url) {
        // VULNERABILITY: Server-Side Request Forgery (SSRF)
        try {
            URL remoteUrl = new URL(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(remoteUrl.openStream()));
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            return content.toString();
            
        } catch (Exception e) {
            return "Error fetching remote file: " + e.getMessage();
        }
    }

    @RequestMapping("/list")
    @ResponseBody
    public String listFiles(@RequestParam(required = false) String dir) {
        // VULNERABILITY: Directory traversal
        String targetDir = (dir != null) ? dir : UPLOAD_DIR;
        
        try {
            File directory = new File(targetDir);
            StringBuilder result = new StringBuilder();
            
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        result.append(file.getName()).append(" - ").append(file.length()).append(" bytes\n");
                    }
                }
            } else {
                result.append("Directory not found: ").append(targetDir);
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Error listing files: " + e.getMessage();
        }
    }
}
