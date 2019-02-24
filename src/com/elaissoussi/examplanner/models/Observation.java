package com.elaissoussi.examplanner.models;

public class Observation {
  
  Exam exam; 
  
  Teacher observer1;
  
  Teacher observer2;

  public Exam getExam() {
    return exam;
  }

  public void setExam(Exam exam) {
    this.exam = exam;
  }

  public Teacher getObserver1() {
    return observer1;
  }

  public void setObserver1(Teacher observer1) {
    this.observer1 = observer1;
  }

  public Teacher getObserver2() {
    return observer2;
  }

  public void setObserver2(Teacher observer2) {
    this.observer2 = observer2;
  }
  
  @Override
  public String toString() {
    return getObserver1().getName() + "("+getObserver1().getObservationCount()+")" +" - "+getObserver2().getName() + "("+getObserver1().getObservationCount()+")";
  }
  
}
