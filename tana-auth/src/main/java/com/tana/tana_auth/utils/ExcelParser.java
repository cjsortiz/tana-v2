package com.tana.tana_auth.utils;

import com.tana.tana_auth.functions.places.dto.PlacesRequestDto;
import com.tana.tana_auth.functions.places.service.PlacesService;

import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.enums.SegmentTypeEnum;
import com.tana.tana_common.constant.exception.TanaException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.tana.tana_common.constant.enums.SegmentTypeEnum.*;

@Service
public class ExcelParser {

    @Autowired
    private PlacesService placesService;

    public void parse(String filePath) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath))) {

            Sheet sheet = workbook.getSheet("ALL DATA");

            if (sheet == null) {
                throw new RuntimeException("Sheet 'DATABASE' not found in Excel file.");
            }

            for (int i = 2; i < 78; i++) { // start at row 3 (skip header)
                Row row = sheet.getRow(i);
                System.out.println("Sheet" + row);
                if (row == null) continue;

                PlacesRequestDto dto = PlacesRequestDto.builder()
                        .name(get(row,0))
                        .overview(get(row,1))
                        .spotId(getSpotId(get(row, 2)))
                        .categoryTypeEnum(get(row,3))
                        .subCategoryTypeEnum(split(get(row,4)))
                        .collections(split(get(row, 5)))
                        .tanaTip(get(row, 24))
                        .town(get(row,14))
                        .gpsLocation(get(row, 15))
                        .openingHours(get(row, 17))
                        .openingDays(get(row, 19))
                        .facebook(get(row, 20))
                        .instagram(get(row, 21))
                        .build();

                System.out.println("Meta Data = " + dto);

                placesService.createPlaces(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel parsing failed", e);
        }
    }

    // ---------- Helpers ----------

    private static String getSpotId(String val) throws TanaException{
        switch (val) {
            case "Nature & Scenery" -> {
                return "1";
            }
            case "Community & Culture" -> {
                return "2";
            }
            case "Food & Drink" -> {
                return "3";
            }
            case "Sports & Wellness" -> {
                return "4";
            }
            case "Events" ->{
                return "5";
            }
            default -> throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
    }

    private static String get(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private static List<String> split(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    private static List<String> splitHash(String text) {
        if (text == null) return List.of();
        return Arrays.stream(text.split(","))
                .map(s -> s.replace("#","").trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static Map<String,String> parseSocials(String text){
        if(text == null) return Map.of();

        Map<String,String> map = new HashMap<>();

        if(text.contains("facebook") || text.contains("FB"))
            map.put("facebook", text);

        if(text.contains("@"))
            map.put("instagram", text);

        return map;
    }

    private static Boolean parseVerified(String text){
        if(text == null) return false;
        return text.toLowerCase().contains("verified");
    }
}
