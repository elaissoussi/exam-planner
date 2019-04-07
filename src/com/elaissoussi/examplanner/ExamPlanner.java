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
import java.util.Set;
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

    String inputFilePath = "./exam-plan.xlsx";
    
    File file = new File(inputFilePath);
    
    System.out.println("### 1- start generating exam plan ###");
    
    // Load data
    Map<Double, Material> materials = loadMaterials(file);

    Map<Double, Teacher> teachers = loadTeachers(file, materials);

    Map<Double, Exam> exams = loadExams(file, materials);
    
    // Exams observations
    Map<Exam, List<Observation>> examsObservations = new HashMap<>();
    
    // Compute max Number Of Observations
    double totalRequiredExamRooms = 0;
    for (Entry<Double, Exam> examEntry : exams.entrySet()) {
      Exam exam = examEntry.getValue();
      // get of total required class by exam
      totalRequiredExamRooms += exam.getNumberOfRomms();
    }
    
    // Max total Observations for teacher
    double maxTotalObservations = Math.ceil(totalRequiredExamRooms / teachers.size()) * 2 ;
    
    // Generate plan
    for (Entry<Double, Exam> examEntry : exams.entrySet()) {

      // Get Material
      Exam exam = examEntry.getValue();
      Material material = exam.getMaterial();

      // Get Teachers G1 & G2
      List<Teacher> teachersGroup1 = getTeachersFor(1, material, teachers);
      List<Teacher> teachersGroup2 = getTeachersFor(2, material, teachers);

      // Max Teacher Group, to get max couple of teachers by exam
      double maxTeacherGroup = Math.min(teachersGroup1.size(), teachersGroup2.size());

      // Max Teacher Observations by exam
      double maxObservations = Math.min(maxTeacherGroup, exam.getNumberOfRomms());
      
      // list of observations
      List<Observation> observations = new ArrayList<>();

      // Generate exam observations 
      for (Teacher teacher1 : teachersGroup1) {
        
        if (!isAlreadyObserverInSameExamGroupe(examsObservations, exam, teacher1)
            && !isAlreadyObserverInCurrentExam(observations, teacher1)
            && observations.size() < maxObservations 
            && teacher1.getObservationCount() < maxTotalObservations) {

          for (Teacher teacher2 : teachersGroup2) {
            if (!isAlreadyObserverInSameExamGroupe(examsObservations, exam, teacher2)
                && !isAlreadyObserverInCurrentExam(observations, teacher2)
                && teacher2.getObservationCount() < maxTotalObservations) {
              
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
    }
  
    generateExamPlan(examsObservations, teachers, maxTotalObservations, file);
    
    System.out.println("### 2- End generating exam plan ###");
    System.out.println("### 3- open plan.xlsx ###");

  }

  private static void generateExamPlan(Map<Exam, List<Observation>> examsObservations, Map<Double, Teacher> teachers, double maxNumberOfObservations, File inputFile) throws Exception {
    
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
    
    // Generate observation statistics
    XSSFSheet observationSheet = workbook.createSheet("Observations statistics");
    int rowStatsNum = 0;
    Row maxStatsRow = observationSheet.createRow(rowStatsNum++);
    Cell statsCell = maxStatsRow.createCell(0);
    statsCell.setCellValue("Max Observations = " + maxNumberOfObservations);
    
    for (Entry<Double, Teacher> examEntry : teachers.entrySet()) {
      Teacher teacher = examEntry.getValue();
      Row teacherStatsRow = observationSheet.createRow(rowStatsNum++);
      
      Cell teacherStatsCell1 = teacherStatsRow.createCell(0);
      teacherStatsCell1.setCellValue(teacher.getName());
      
      Cell teacherStatsCell2 = teacherStatsRow.createCell(1);
      teacherStatsCell2.setCellValue(teacher.getObservationCount());
    }
      
    FileOutputStream outputStream = new FileOutputStream(inputFile.getParent( )+"/plan.xlsx");
    workbook.write(outputStream);
    workbook.close();
  }
  
  private static boolean isAlreadyObserverInCurrentExam(List<Observation> observations, Teacher teacher) {
    
    for (Observation observation : observations) {
      
      Teacher observer1 = observation.getObserver1();
      Teacher observer2 = observation.getObserver2();
      if(observer1.equals(teacher) || observer2.equals(teacher)) {
        return true; 
      }
    }
    return false; 
  }
  
  private static final boolean isAlreadyObserverInSameExamGroupe(Map<Exam, List<Observation>> examsObservations, Exam exam, Teacher teacher) {
    
    // get exams set
    Set<Exam> exams = examsObservations.keySet();
    
    for (Exam exm : exams) {
      // the current exam has the same group of an exam in the list
      if(exm.getGroup() == exam.getGroup()) {
        // get observations of the exam
        List<Observation> observations = examsObservations.get(exm);
        
        if(isAlreadyObserverInCurrentExam(observations, teacher)) {
          return true;
        }
        
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

      // required rooms for this exam
      double numberOfStudents = currentRow.getCell(3).getNumericCellValue();
      double numberOfRomms = Math.ceil(numberOfStudents / STUDENT_NUMBER_ROOM);
      exam.setNumberOfRomms(numberOfRomms);
      
      double examGroupe = currentRow.getCell(4).getNumericCellValue();
      exam.setGroup(examGroupe);
      
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
