package com.elaissoussi.examplanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.elaissoussi.examplanner.models.Exam;
import com.elaissoussi.examplanner.models.Material;
import com.elaissoussi.examplanner.models.Observation;
import com.elaissoussi.examplanner.models.Teacher;

public class ExamPlanner {

  private static final int STUDENT_NUMBER_ROOM = 20;
    
  public static void main(String[] args) throws Exception {

    final String inputFile = args[0];

    File file = new File(inputFile);

    // Load data
    Map<Double, Material> materials = loadMaterials(file);

    Map<Double, Teacher> teachers = loadTeachers(file, materials);

    Map<Double, Exam> exams = loadExams(file, materials);
    
    // Exams observations
    Map<Exam, List<Observation>> examsObservations = new HashMap<>();
    
    // Compute max Number Of Observations
    double numberOfRomms = 0;
    for (Entry<Double, Exam> examEntry : exams.entrySet()) {
      Exam exam = examEntry.getValue();
      numberOfRomms += exam.getNumberOfRomms();
    }

    double maxNumberOfObservations = Math.ceil(numberOfRomms / teachers.size())*2;

    // Generate plan
    for (Entry<Double, Exam> examEntry : exams.entrySet()) {

      // Get Material
      Exam exam = examEntry.getValue();
      Material material = exam.getMaterial();

      // Get Teachers G1 & G2
      List<Teacher> teachersGroup1 = getTeachersFor(1, material, teachers);
      List<Teacher> teachersGroup2 = getTeachersFor(2, material, teachers);

      // Max Teacher Group
      double maxTeacherGroup = Math.min(teachersGroup1.size(), teachersGroup2.size());

      // Max Teacher Observations
      double maxObservations = Math.min(maxTeacherGroup, exam.getNumberOfRomms());

      // list of observations
      List<Observation> observations = new ArrayList<>();

      // Generate exam observations 
      for (Teacher teacher1 : teachersGroup1) {
        
        if (!isAlreadyObserver(observations, teacher1)
            && observations.size() < maxObservations 
            && teacher1.getObservationCount() < maxNumberOfObservations) {

          for (Teacher teacher2 : teachersGroup2) {
            if (!isAlreadyObserver(observations, teacher2) 
                && teacher2.getObservationCount() < maxNumberOfObservations) {
              
              // increment number of observation
              teacher1.setObservationCount(teacher1.getObservationCount() + 1 );
              teacher2.setObservationCount(teacher2.getObservationCount() + 1 );
              
              // create a observation 
              Observation observation = new Observation();
              observation.setExam(exam);
              observation.setObserver1(teacher1);
              observation.setObserver2(teacher2);
              observations.add(observation);

              break;
              
            }
          }
        }
      }
      
      examsObservations.put(exam, observations);
      
      /*System.out.println("Exam => " + exam.getName());
      System.out.println("Exam => " + exam.getNumberOfRomms());

      for (Observation obs : observations) {
        System.out.println(obs);
      }
      */
      
    }
   
     /* System.out.println("Max observation => " + maxNumberOfObservations);
      for (Entry<Double, Teacher> examEntry : teachers.entrySet()) {
        Teacher t = examEntry.getValue();
        System.out.println(t.getName() + " = " + t.getObservationCount());
      }
    */
    // write excel file
    
    generateExamPlan(examsObservations, file);
    
  }

  private static void generateExamPlan(Map<Exam, List<Observation>> examsObservations, File inputFile) throws Exception {
    
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Exam Plan");
    
    int rowNum = 0;
    
    for(Entry<Exam, List<Observation>> observationEntry : examsObservations.entrySet()) {
      
      Exam exam = observationEntry.getKey();
      
      Row row = sheet.createRow(rowNum++);
      Cell cell = row.createCell(0);
      cell.setCellValue(exam.getName() +  " - " + exam.getMaterial().getName() +" - " + exam.getNumberOfRomms());
      
      List<Observation> observations = observationEntry.getValue();
      
      for (Observation observation : observations) {
        Row obsRow = sheet.createRow(rowNum++);
        
        Cell cellObser1 = obsRow.createCell(1);
        cellObser1.setCellValue(observation.getObserver1().getName());
        
        Cell cellObser2 = obsRow.createCell(2);
        cellObser2.setCellValue(observation.getObserver2().getName());
      }
    }
    
    FileOutputStream outputStream = new FileOutputStream(inputFile.getParent( )+"/plan.xlsx");
    workbook.write(outputStream);
    workbook.close();
    
  }
  
  private static boolean isAlreadyObserver(List<Observation> observations, Teacher teacher) {
    
    for (Observation observation : observations) {
      Teacher observer1 = observation.getObserver1();
      Teacher observer2 = observation.getObserver2();
      if(observer1.equals(teacher) || observer2.equals(teacher)) {
        return true; 
      }
    }
    return false; 
  }
  
  private static List<Teacher> getTeachersFor(double groupe, Material material,
      Map<Double, Teacher> teachers) {

    List<Teacher> teacherList = new ArrayList<>();

    for (Entry<Double, Teacher> teacherEntry : teachers.entrySet()) {
      Teacher teacher = teacherEntry.getValue();
      Material teacherMaterial = teacher.getMaterial();

      if (teacher.getGroup() == groupe && !teacherMaterial.equals(material)) {
        teacherList.add(teacher);
      }
    }
    
    // put teachers with less observation count in first 
    teacherList.sort(Comparator.comparingDouble(Teacher::getObservationCount));
    
    return teacherList;
  }


  private static Map<Double, Teacher> loadTeachers(File file, Map<Double, Material> materials) throws Exception {

    FileInputStream excelFile = new FileInputStream(file);
    Workbook workbook = new XSSFWorkbook(excelFile);
    Sheet materialSheet = workbook.getSheetAt(2);
    Iterator<Row> examIterator = materialSheet.iterator();

    Map<Double, Teacher> teachers = new HashMap<>();

    // Skip header
    examIterator.next();

    while (examIterator.hasNext()) {

      Row currentRow = examIterator.next();
      Teacher teacher = new Teacher();

      double teacherId = currentRow.getCell(0).getNumericCellValue();
      teacher.setId(teacherId);

      teacher.setName(currentRow.getCell(1).getStringCellValue());

      double materialId = currentRow.getCell(2).getNumericCellValue();
      teacher.setMaterial(materials.get(materialId));

      double group = currentRow.getCell(3).getNumericCellValue();
      teacher.setGroup(group);

      teachers.put(teacherId, teacher);
    }

    return teachers;
  }

  private static Map<Double, Exam> loadExams(File file, Map<Double, Material> materials) throws Exception {
    
    FileInputStream excelFile = new FileInputStream(file);
    Workbook workbook = new XSSFWorkbook(excelFile);
    Sheet materialSheet = workbook.getSheetAt(1);
    Iterator<Row> examIterator = materialSheet.iterator();

    Map<Double, Exam> exams = new HashMap<>();

    // Skip header
    examIterator.next();

    while (examIterator.hasNext()) {

      Row currentRow = examIterator.next();
      Exam exam = new Exam();

      double examId = currentRow.getCell(0).getNumericCellValue();
      exam.setId(examId);

      exam.setName(currentRow.getCell(1).getStringCellValue());

      double materialId = currentRow.getCell(2).getNumericCellValue();
      exam.setMaterial(materials.get(materialId));


      double numberOfStudents = currentRow.getCell(3).getNumericCellValue();
      double numberOfRomms = Math.ceil(numberOfStudents / STUDENT_NUMBER_ROOM);
      exam.setNumberOfRomms(numberOfRomms);

      exams.put(examId, exam);
    }

    return exams;
  }

  private static Map<Double, Material> loadMaterials(File file) throws Exception {

    FileInputStream excelFile = new FileInputStream(file);
    Workbook workbook = new XSSFWorkbook(excelFile);
    Sheet materialSheet = workbook.getSheetAt(0);
    Iterator<Row> materialIterator = materialSheet.iterator();

    Map<Double, Material> materials = new HashMap<>();

    // Skip header
    materialIterator.next();

    while (materialIterator.hasNext()) {

      Row currentRow = materialIterator.next();
      Material material = new Material();

      double materialId = currentRow.getCell(0).getNumericCellValue();
      material.setId(materialId);
      material.setName(currentRow.getCell(1).getStringCellValue());
      materials.put(materialId, material);
    }

    return materials;
  }

}
