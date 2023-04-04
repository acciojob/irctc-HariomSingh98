package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        //validate the train and check if seats are available

        List<Ticket> tickets = train.getBookedTickets();
        int alreadyBookedSeats =0;
        for(Ticket t : tickets){
            alreadyBookedSeats+=t.getPassengersList().size();
        }

        if(alreadyBookedSeats+bookTicketEntryDto.getNoOfSeats()>train.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        //check if train pass through required stations
        int x = -1;//index of fromStation in train route

        String []routes = train.getRoute().split(",");

        for(int i=0;i<routes.length;i++){
            if(routes[i].equalsIgnoreCase(bookTicketEntryDto.getFromStation().toString())){
                x=i;
                break;
            }
        }
        int y=-1;//index of toStation in our train route
        for(int i=0;i<routes.length;i++){
            if(routes[i].equalsIgnoreCase(bookTicketEntryDto.getToStation().toString()) && i>x){
                y=i;
                break;
            }
        }

        if(x==-1||y==-1){//in case any one of them is -1
            throw new Exception("Invalid stations");
        }

        //create new ticket object to book ticket and set the attribute
        Ticket newTicket = new Ticket();
        int totalFare =0;
        totalFare= bookTicketEntryDto.getPassengerIds().size()*(y-x)*300;//set the total fare

        newTicket.setFromStation(bookTicketEntryDto.getFromStation());
        newTicket.setToStation(bookTicketEntryDto.getToStation());

        newTicket.setTotalFare(totalFare);
        newTicket.setTrain(train);

        //get the list of passenger
        List<Passenger> passengers = new ArrayList<>();
        List<Integer> list =bookTicketEntryDto.getPassengerIds();
        for(int i : list){
            Passenger passenger = passengerRepository.findById(i).get();
            passengers.add(passenger);
        }
        newTicket.setPassengersList(passengers);


        train.getBookedTickets().add(newTicket);

        Passenger bookedPerson = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        bookedPerson.getBookedTickets().add(newTicket);

        Train savedTrain = trainRepository.save(train);

        int ticketId = savedTrain.getBookedTickets().get(savedTrain.getBookedTickets().size()-1).getTicketId();


        return ticketId;

    }
}
