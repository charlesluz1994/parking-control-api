package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {
    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){
        //Validations before save, after conclude, isolate in a custom validator.
        if(parkingSpotService.existsLicensePlateCar(parkingSpotDto.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot already registered for this apartment/block!");
        }

        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModeOptional = parkingSpotService.findById(id);
        if(!parkingSpotModeOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        parkingSpotService.delete(parkingSpotModeOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully");
    }
    @PutMapping("/{id}") //recebe dto e precosa convweter para model
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
                                                    @RequestBody @Valid ParkingSpotDto parkingSpotDto){
        Optional<ParkingSpotModel> parkingSpotModeOptional = parkingSpotService.findById(id);
        if(!parkingSpotModeOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }

        //var parkingSportModel = parkingSpotModeOptional.get();
        //ID and Registration date keep the same, but all fields ou one, be update if have some alteration.
        /*
        parkingSportModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
        parkingSportModel.setLicensePlateCar(parkingSpotDto.getLicensePlateCar());
        parkingSportModel.setModelCar(parkingSpotDto.getModelCar());
        parkingSportModel.setBrandCar(parkingSpotDto.getBrandCar());
        parkingSportModel.setColorCar(parkingSpotDto.getColorCar());
        parkingSportModel.setResponsibleName(parkingSpotDto.getResponsibleName());
        parkingSportModel.setApartment(parkingSpotDto.getApartment());
        parkingSportModel.setBlock(parkingSpotDto.getBlock());*/

        /*Instead of getting the Optional and setting all fields at once as in the example above, we can do it as below
        *Create an instance of parkingSportModel. Convert Dto to Model using BeanUtils.
        *And set only the Id to remain the same as what we got from the BD. And also the registration date that is already saved in the BD.
         */
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotModeOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModeOptional.get().getRegistrationDate());

        //Using the method save to pass the new SpotModel to be updated in the database.
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }

}
