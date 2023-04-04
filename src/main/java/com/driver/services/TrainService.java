package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        List<Station> list = trainEntryDto.getStationRoute();
        String route ="";
        for(int i=0;i<list.size();i++){
            if(i==list.size()-1)route+=list.get(i).toString();
            else route+=list.get(i).toString()+",";
        }

        train.setRoute(route);

        Train savedTrain =  trainRepository.save(train);

        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        Station fromStation = seatAvailabilityEntryDto.getFromStation();
        Station toStation = seatAvailabilityEntryDto.getToStation();

        String []routes = train.getRoute().split(",");
        int x =-1;//index of  fromStation in train route

        int y=-1;//index of toStation in train route
        for(int i=0;i< routes.length;i++){
           if(routes[i].equalsIgnoreCase(fromStation.toString()))x=i;

           if(routes[i].equalsIgnoreCase(toStation.toString()))y=i;
        }

        int availableSeats = 0;

        int totalTrainSeats = train.getNoOfSeats();//get no of seats in train

        int bookedSeats = 0;

        List<Ticket> ticketList = train.getBookedTickets();

        for(Ticket t : ticketList){
            List<Passenger> passengers = t.getPassengersList();
            Station starting = t.getFromStation();
            Station ending = t.getToStation();
            int z = -1;
            int w =-1;
            for(int i=0;i< routes.length;i++){
                if(routes[i].equalsIgnoreCase(starting.toString()))z=i;

                if(routes[i].equalsIgnoreCase(ending.toString()))w=i;
            }

            //check the overlapping condition marked as booked seats
            if((z<x && w>x) || (z==x && w<y))bookedSeats+=passengers.size();


        }

        availableSeats = totalTrainSeats - bookedSeats;

        return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();

        String []routes = train.getRoute().split(",");
        boolean isPresent =false;
        for(String s : routes){
            if(station.toString().equalsIgnoreCase(s)){
                isPresent=true;
                break;
            }
        }

        if(isPresent==false)throw new Exception("Train is not passing from this station");

        int totalPassenger= 0;

        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket t : tickets){
            if(t.getFromStation().equals(station)){
                totalPassenger+=t.getPassengersList().size();
            }
        }

        return totalPassenger;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train = trainRepository.findById(trainId).get();

        List<Ticket> tickets = train.getBookedTickets();

        if(tickets.isEmpty()==true)return 0;

        int oldest = 0;
        for(Ticket t : tickets){
            List<Passenger> passengers = t.getPassengersList();
            for(Passenger p : passengers){
                if(oldest<p.getAge())oldest=p.getAge();
            }
        }

        return oldest;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trains = trainRepository.findAll();//get the list of trains

        List<Integer> trainIds = new ArrayList<>();

        for(Train t : trains){

            if(t.getRoute().contains(station.toString())){
                LocalTime time = t.getDepartureTime();
                if(time.compareTo(startTime)>=0 && time.compareTo(endTime)<=0){
                     trainIds.add(t.getTrainId());
                }
            }
        }

       return trainIds;

    }

}
