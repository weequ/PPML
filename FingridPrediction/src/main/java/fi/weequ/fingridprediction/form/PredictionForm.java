package fi.weequ.fingridprediction.form;

import javax.validation.constraints.NotNull;


public class PredictionForm {
    @NotNull
    private Double helsinkiTemp;
    @NotNull
    private Double kuopioTemp;
    @NotNull
    private Double rovaniemiTemp;
    @NotNull
    private Integer dayOfWeek;
    @NotNull
    private Integer hourOfDay;
    
    public PredictionForm() {
        
    }
    
    public Double getHelsinkiTemp() {
        return helsinkiTemp;
    }

    public void setHelsinkiTemp(Double helsinkiTemp) {
        this.helsinkiTemp = helsinkiTemp;
    }

    public Double getKuopioTemp() {
        return kuopioTemp;
    }

    public void setKuopioTemp(Double kuopioTemp) {
        this.kuopioTemp = kuopioTemp;
    }

    public Double getRovaniemiTemp() {
        return rovaniemiTemp;
    }

    public void setRovaniemiTemp(Double rovaniemiTemp) {
        this.rovaniemiTemp = rovaniemiTemp;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(Integer hourOfDay) {
        this.hourOfDay = hourOfDay;
    }
    
    
    
}