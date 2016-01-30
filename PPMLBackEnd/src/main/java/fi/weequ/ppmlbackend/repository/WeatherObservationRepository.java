package fi.weequ.ppmlbackend.repository;

import fi.weequ.ppmlbackend.domain.WeatherObservation;
import org.springframework.data.repository.CrudRepository;

public interface WeatherObservationRepository extends CrudRepository<WeatherObservation, Long> {
    
}
