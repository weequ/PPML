package fi.weequ.ppmlbackend.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table
public class WeatherObservation extends BaseEntity {
    
    @Column(nullable = false)
    Date time;
    
    @Column
    Long fmisid;
    @Column
    String name;
    @Column
    Long geoid;
    @Column
    Long wmo;
    
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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Long getFmisid() {
        return fmisid;
    }

    public void setFmisid(Long fmisid) {
        this.fmisid = fmisid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGeoid() {
        return geoid;
    }

    public void setGeoid(Long geoid) {
        this.geoid = geoid;
    }

    public Long getWmo() {
        return wmo;
    }

    public void setWmo(Long wmo) {
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
        return time+"=rrday: "+rrday+", temp: "+ttday+", snow: "+snow+", min temp:"+tmin+", max temp"+tmax;
    }
    
}
