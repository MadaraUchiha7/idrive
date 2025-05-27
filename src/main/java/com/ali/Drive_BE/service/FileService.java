package com.ali.Drive_BE.service;

import com.ali.Drive_BE.entity.FileEntity;
import com.ali.Drive_BE.repo.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public String saveFile(MultipartFile  file, Long parentFolderId) throws IOException {

        Path uploadPath = Paths.get(uploadDir);
        try {
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        //meta data for db

        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(fileName);
        fileEntity.setPath(filePath.toString());
        fileEntity.setSize(file.getSize());
        fileEntity.setType("file");
        fileEntity.setParentFolderId(parentFolderId);
        fileEntity.setCreatedAt(LocalDateTime.now());

        fileRepository.save(fileEntity);

        return "File uploaded Successfully!";
    }

    public List<FileEntity> getFilesInFolder(Long parentFolderId){
        if(parentFolderId == null){
            return fileRepository.findAll().stream().filter(f -> f.getParentFolderId() == null).collect(Collectors.toList());
        }
        else{
            return fileRepository.findAll().stream().filter(f -> f.getParentFolderId().equals(parentFolderId)).collect(Collectors.toList());
        }
    }

    public  FileEntity getFilesById(Long id){
        return fileRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

    public void  deleteById(Long id){
        fileRepository.deleteById(id);
    }
}
