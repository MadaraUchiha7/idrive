package com.ali.Drive_BE.controller;


import com.ali.Drive_BE.entity.FileEntity;
import com.ali.Drive_BE.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file, @RequestParam(value = "parentFolderId", required = false )Long parentFolderId){
        try{
            String respomse = fileService.saveFile(file, parentFolderId);
            return ResponseEntity.ok(respomse);
        }
        catch (Exception e){
            return ResponseEntity.status(500).body("File upload failed!");
        }
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id){
        try{
            //mysql meta data
            FileEntity fileEntity = fileService.getFilesById(id);
            Path path = Paths.get(fileEntity.getPath());
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok().header("content-Disposition", "attachment; filename=\""+fileEntity.getName()+"\"").body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
    @GetMapping("/list")
    public ResponseEntity<List<FileEntity>> listfiles(@RequestParam(value = "parentFolderId", required = false) Long parentFolderId){
        List<FileEntity> files;
        if(parentFolderId == null){
           files = fileService.getFilesInFolder(null);
        }
        else{
            files = fileService.getFilesInFolder(parentFolderId);
        }
        return  ResponseEntity.ok(files);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id){
        try{
            // we will take path from mysql
            FileEntity fileEntity = fileService.getFilesById(id);
            //will make path and delete from disk
            Path path = Paths.get(fileEntity.getPath());
            Files.deleteIfExists(path);
            fileService.deleteById(id);
            return ResponseEntity.ok("File deleted Sucessfully!");

        } catch (Exception e) {
            return  ResponseEntity.status(500).body("Failed to delete file!");
        }
    }
}
