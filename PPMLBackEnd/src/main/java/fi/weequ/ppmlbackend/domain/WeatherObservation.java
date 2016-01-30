package fi.weequ.ppmlbackend.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;


@Entity
@Table(uniqueConstraints=
           @UniqueConstraint(columnNames = {"date", "wmo"})) 
public class WeatherObservation extends BaseEntity {
    
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    Date date;
    
    @Column
    String wmo;
    
    @Column
    Double rrday;//sade
    @Column
    Double ttday;//keskilämpötila
    @Column
    Double snow;//lumen paksuus
    @Column
    Double tmin;//minimilämpötila
    @Column
    Double tmax;//maximilämpötila

    public Date getDate() {
        return date;
    }

    public void setDate(Date time) {
        this.date = time;
    }

    public String getWmo() {
        return wmo;
    }

    public void setWmo(String wmo) {
        this.wmo = wmo;
    }

    public Double getRrday() {
        return rrday;
    }

    public void setRrday(Double rrday) {
        this.rrday = rrday;
    }

    public Double getTtday() {
        return ttday;
    }

    public void setTtday(Double ttday) {
        this.ttday = ttday;
    }

    public Double getSnow() {
        return snow;
    }

    public void setSnow(Double snow) {
        this.snow = snow;
    }

    public Double getTmin() {
        return tmin;
    }

    public void setTmin(Double tmin) {
        this.tmin = tmin;
    }

    public Double getTmax() {
        return tmax;
    }

    public void setTmax(Double tmax) {
        this.tmax = tmax;
    }

    
    @Override
    public String toString() {
        return date+"=rrday: "+rrday+", temp: "+ttday+", snow: "+snow+", min temp:"+tmin+", max temp:"+tmax;
    }
    
}
