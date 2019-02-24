package com.elaissoussi.examplanner.models;

public class Teacher {
  
  private double id; 
  
  private String name; 
  
  private Material material ; 
  
  private double group;
  
  private double observationCount; 
  
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

  public double getGroup() {
    return group;
  }

  public void setGroup(double group) {
    this.group = group;
  }

  public double getObservationCount() {
    return observationCount;
  }

  public void setObservationCount(double observationCount) {
    this.observationCount = observationCount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(id);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Teacher other = (Teacher) obj;
    if (Double.doubleToLongBits(id) != Double.doubleToLongBits(other.id))
      return false;
    return true;
  }
 
}
