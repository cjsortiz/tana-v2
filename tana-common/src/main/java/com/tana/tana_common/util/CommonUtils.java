package com.tana.tana_common.util;

import com.tana.tana_common.constant.CommonConstants;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.TanaDateFormat;
import com.tana.tana_common.constant.enums.TanaFileName;
import com.tana.tana_common.constant.exception.TanaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommonUtils {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private CacheManager cacheManager;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    public static String getUploadImage(Object id, String fileDirectory, String fileName) throws TanaException {
        try {
            java.nio.file.Path fullFileDirectory = Paths.get(fileDirectory, id.toString());
            String fullFileName = fullFileDirectory + File.separator + fileName;

            // Check if the file exists
            java.nio.file.Path filePath = Paths.get(fullFileName);
            if (!Files.exists(filePath)) {
                return null; // Return null if the file does not exist
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException o) {
            return null; // Return null in case of an IOException
        }
    }

    /***
     * Generates a file name
     *
     * @param name the {@link TanaFileName} enum value to use as the base of the
     *             file name
     * @param file the object to generate the file name for
     * @return representing the generated file name
     * @deprecated new update is available please use that instead
     */
    @Deprecated
    public static String generateFileName(TanaFileName name, MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TanaDateFormat.YYYYMMDDHHMMSS.getValue());
        String formattedDateTime = LocalDateTime.now().format(formatter);
        return String.format(CommonConstants.FILE_NAME,
                name.getValue(),
                formattedDateTime,
                extension);
    }


    /**
     * Parses a comma-separated string into a list of strings.
     * Trims whitespace from each item and handles empty strings.
     *
     * @param input The comma-separated string to parse.
     * @return A list of strings, or an empty list if input is null or empty.
     */
    public static List<String> parseCommaSeparatedString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Split by comma, trim each part, and filter out empty strings
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Validates the fields of an object based on regular expressions and excluded fields.
     *
     * @param dto The object to validate.
     * @throws TanaException If the input is invalid.
     */
    public <T> T validateObjectData(T dto) throws TanaException {

        try {
            Class<?> currentClass = dto.getClass();
            List<Field> allFields = getAllFields(currentClass);
            for (Field field : allFields) {
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value instanceof Set<?> set) {
                    Set<Object> formattedStringSet = new HashSet<>();
                    for (Object element : set) {
                        formattedStringSet.add(validateObjectData(element));

                    }
                    field.set(dto, formattedStringSet);

                } else if (value instanceof List<?> list) {
                    List<Object> formattedStringSet = new ArrayList<>();
                    for (Object element : list) {
                        Class<?> clazzType = element.getClass();
                        if (clazzType.getPackageName().endsWith(".dto")) {
                            formattedStringSet.add(validateObjectData(element));
                        } else {
                            formattedStringSet.add(validateDefaultDatatype(element, null));
                        }

                    }
                    field.set(dto, formattedStringSet);

                }

            }
            return dto;
        } catch (IllegalAccessException e) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
    }

    /**
     * Retrieves all fields from the given class and its superclasses.
     *
     * @param clazz The class for which to retrieve fields.
     * @return A list containing all fields from the class and its superclasses.
     */
    private List<Field> getAllFields(Class<?> clazz) {

        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();

        while (superClass != null) {
            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();

        }
        return fields;

    }

    /**
     * Validates and formats the input value for default datatypes such as String, Integer, Long, etc.
     * Handles special characters and ensures a maximum length of 256 characters.
     *
     * @param stringValue The input value to be validated and formatted.
     * @param requestType The type of request (e.g., REQUEST or RESPONSE).
     * @return The validated and formatted input value.
     * @throws TanaException If special characters are present or if the length exceeds 256 characters.
     */
    public String validateDefaultDatatype(Object stringValue, String requestType) {
        for (String specialChar : CommonConstants.SPECIAL_CHARS) {
            String doubledSpecialChar = CommonConstants.ESC_CHAR + specialChar;

            if (Objects.equals(requestType, CommonConstants.RESPONSE)) {
                if (stringValue.toString().contains(specialChar)) {
                    return stringValue.toString().replace(doubledSpecialChar, specialChar);
                } else if (stringValue.toString().contains(CommonConstants.ESC_CHAR)) {
                    return stringValue.toString().replace(CommonConstants.ESC_CHAR, CommonConstants.EMPTY_STRING);
                }
            }

            // Check if stringValue contains a special character that is not escaped
            if (stringValue.toString().contains(specialChar) && !stringValue.toString().contains(doubledSpecialChar)) {
                throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
            }

        }

        if (stringValue.toString().length() > 255) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }

        return stringValue.toString();
    }

    @CacheEvict(value = "spot-images", key = "#folderName", condition = "#rootFolder == 'tana-place-images'")
    public String uploadImage(
        String userName,
        Long userId,
        String folderName,
        MultipartFile file,
        String rootFolder
    ) {
        try {
            String safeRootFolder = rootFolder == null
                ? ""
                : rootFolder.replaceAll("[^a-zA-Z0-9-_]", "");

            String safeFolder = folderName.replaceAll("[^a-zA-Z0-9-_]", "");

            String safeUsername = userName == null
                ? "user"
                : userName.replaceAll("[^a-zA-Z0-9-_]", "");

            String originalName = file.getOriginalFilename();
            String ext = "";

            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName =
                safeUsername + "-" + userId + "-" + System.currentTimeMillis() + ext;

            String key = safeRootFolder.isBlank()
                ? safeFolder + "/" + fileName
                : safeRootFolder + "/" + safeFolder + "/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .cacheControl("public, max-age=31536000, immutable")
                .build();

            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(file.getBytes())
            );

            return key;

        } catch (Exception e) {
            log.error("Upload Failed", e);
            throw new RuntimeException("Upload failed", e);
        }
    }

    public void deleteImage(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        } catch (Exception exception) {
            log.error("S3 image deletion failed for key {}", key, exception);
        }
    }


    public List<String> getSpotImages(String spotName) {
        if (spotName == null || spotName.isBlank()) return Collections.emptyList();
        Cache cache = cacheManager.getCache("spot-images");
        // Explicit cache access also covers calls from the batch method in this class.
        return cache == null ? loadSpotImages(spotName)
            : cache.get(spotName, () -> loadSpotImages(spotName));
    }

    private List<String> loadSpotImages(String spotName) {
        String sanitizedName = spotName.replaceAll("[^a-zA-Z0-9-_]", "");
        String prefix = "tana-place-images/" + sanitizedName + "/";

        try {
            List<String> images = new ArrayList<>();
            String token = null;
            do {
                ListObjectsV2Response response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucketName).prefix(prefix).continuationToken(token).build());
                response.contents().stream().map(S3Object::key)
                    .filter(key -> !key.endsWith("/")).forEach(images::add);
                token = Boolean.TRUE.equals(response.isTruncated()) ? response.nextContinuationToken() : null;
            } while (token != null);
            return List.copyOf(images);

        } catch (Exception e) {
            log.error("Failed to fetch spot images from S3", e);
            return Collections.emptyList();
        }
    }

    public Map<String, List<String>> getSpotImagesBySpotNames(Collection<String> spotNames) {
        if (spotNames == null || spotNames.isEmpty()) {
            return Collections.emptyMap();
        }

        return spotNames.stream().filter(Objects::nonNull).filter(name -> !name.isBlank())
            .distinct().collect(Collectors.toMap(name -> name, this::getSpotImages));
    }
}
