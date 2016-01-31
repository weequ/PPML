package fi.weequ.ppmlbackend.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ElectricityObservation extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date finnishTime;
    
    @Column
    private Double electricityConsumption;
    
    @Column
    private Double electricityProduction;

    public Date getFinnishTime() {
        return finnishTime;
    }

    public void setFinnishTime(Date finnishTime) {
        this.finnishTime = finnishTime;
    }

    public Double getElectricityConsumption() {
        return electricityConsumption;
    }

    public void setElectricityConsumption(Double electricityConsumption) {
        this.electricityConsumption = electricityConsumption;
    }

    public Double getElectricityProduction() {
        return electricityProduction;
    }

    public void setElectricityProduction(Double electricityProduction) {
        this.electricityProduction = electricityProduction;
    }

}
