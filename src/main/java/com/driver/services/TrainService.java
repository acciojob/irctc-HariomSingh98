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
import java.util.HashMap;
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

        int totalTrainSeats = train.getNoOfSeats();//get no of seats in train

        int bookedSeats = 0;

        List<Ticket> ticketList = train.getBookedTickets();

        for(Ticket t : ticketList){
            bookedSeats+=t.getPassengersList().size();//intial seats that are book
        }

        int availableSeats = train.getNoOfSeats()- bookedSeats;

        //to identify extra seats that can be used
        String []routes = train.getRoute().split(",");
        int a =-1,b=-1;
        for(int i=0;i<routes.length;i++){
            if(seatAvailabilityEntryDto.getFromStation().toString().equalsIgnoreCase(routes[i]))a=i;//index of our target boarding station
            else if(seatAvailabilityEntryDto.getToStation().toString().equalsIgnoreCase(routes[i]))b=i;//index of our target destination station
        }
        if(a==-1 || b==-1|| (b-a)<=0)return 0;

        for(Ticket t : ticketList) {
            int c = -1;
            int d = -1;
            for (int i = 0; i < routes.length; i++) {
                if (t.getFromStation().toString().equalsIgnoreCase(routes[i]))c=i;//index of boarding station of ticket
                else if(t.getToStation().toString().equalsIgnoreCase(routes[i]))d=i;//index of destination station of ticket
            }

            if(c>b)availableSeats+=t.getPassengersList().size();

            else if(d<a)availableSeats+=t.getPassengersList().size();
        }

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

        int startingTime = startTime.getHour()*60+startTime.getMinute();//in minutes according to a day
        int endingTime = endTime.getHour()*60 + endTime.getMinute();//in min acc to a day

        List<Integer> trainIds = new ArrayList<>();


        for(Train t : trains){
            String []routes = t.getRoute().split(",");
            int indexOfStation  = -1;
            for(int i=0;i<routes.length;i++){
                if(routes[i].equalsIgnoreCase(station.toString()))indexOfStation=i;
            }

            int  comingToStationTime = indexOfStation==0?(t.getDepartureTime().getHour()*60+t.getDepartureTime().getMinute())
                    : ((t.getDepartureTime().getHour()+indexOfStation)*60+t.getDepartureTime().getMinute());

            if(comingToStationTime>=startingTime && comingToStationTime<=endingTime){
                trainIds.add(t.getTrainId());
            }
        }

       return trainIds;

    }

}
