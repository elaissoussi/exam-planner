package com.elaissoussi.examplanner.models;

public class Material {

  private double id; 
  
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getId() {
    return id;
  }

  public void setId(double id) {
    this.id = id;
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
    Material other = (Material) obj;
    if (Double.doubleToLongBits(id) != Double.doubleToLongBits(other.id))
      return false;
    return true;
  } 
  
  
}
