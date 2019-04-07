package com.elaissoussi.examplanner.models;

public class Exam {
  
  private double id; 
  private String name ; 
  private Material material; 
  private double numberOfRomms;
  private double group;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Material getMaterial() {
    return material;
  }
  public void setMaterial(Material material) {
    this.material = material;
  }
  public double getId() {
    return id;
  }
  public void setId(double id) {
    this.id = id;
  }
  public double getNumberOfRomms() {
    return numberOfRomms;
  }
  public void setNumberOfRomms(double numberOfRomms) {
    this.numberOfRomms = numberOfRomms;
  }
  public double getGroup() {
    return group;
  }
  public void setGroup(double group) {
    this.group = group;
  }
 
}
