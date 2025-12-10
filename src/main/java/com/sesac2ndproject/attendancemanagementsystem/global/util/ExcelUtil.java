package com.sesac2ndproject.attendancemanagementsystem.global.util;

import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.query.dto.ResponseAttendanceFlatDTO;
import com.sesac2ndproject.attendancemanagementsystem.domain.attendance.common.entity.DetailedAttendance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelUtil {
    public static byte[] createExcelFile(List<ResponseAttendanceFlatDTO> dataList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baout = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("출석부");

            // 헤더 스타일 설정(밑의 헤더 생성에서 사용함)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);   // 폰트(bold)
            headerStyle.setFont(headerFont);

            // 헤더 생성(Row 0)
            Row headerRow = sheet.createRow(0);
            String[] headers = {"날짜", "학생ID", "학생이름", "최종상태", "강좌ID", "강좌이름", "출석타입", "입력시간", "IP", "검증여부"};
            for(int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 생성(Row 1~n)
            int rowIdx = 1;
            for (ResponseAttendanceFlatDTO dto : dataList){
                Row row = sheet.createRow(rowIdx++); // 해당하는 rowIndex로 Row를 만든 후 +1

                row.createCell(0).setCellValue(dto.getWorkDate().toString());
                row.createCell(1).setCellValue(dto.getMemberId());
                row.createCell(2).setCellValue(dto.getMemberName());
                row.createCell(3).setCellValue(dto.getTotalStatus().getDescription());
                row.createCell(4).setCellValue(dto.getCourseId());
                row.createCell(5).setCellValue(dto.getCourseName());
                // 상세 데이터(detailedAttendance) 존재 여부에 따라 넣을 값 결정
                if(dto.getDetailedAttendance() != null) {
                    DetailedAttendance detail = dto.getDetailedAttendance();
                    row.createCell(6).setCellValue(detail.getType().toString());
                    row.createCell(7).setCellValue(detail.getCheckTime().toString());
                    row.createCell(8).setCellValue(detail.getConnectionIp());
                    row.createCell(9).setCellValue(detail.isVerified() ? "성공" : "실패");
                } else {
                    // 상세 데이터가 없을 경우 빈칸
                    row.createCell(6).setCellValue("-");
                    row.createCell(7).setCellValue("-");
                    row.createCell(8).setCellValue("-");
                    row.createCell(9).setCellValue("-");
                }
            }

            // 컬럼 너비 자동 조절 로직
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 여유있게 현재 너비에 20% 여유 너비를 추가
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, (int)(currentWidth * 1.2));
            }

            workbook.write(baout);
            return baout.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Excel 파일 생성 실패, e");
        }
    }
}
