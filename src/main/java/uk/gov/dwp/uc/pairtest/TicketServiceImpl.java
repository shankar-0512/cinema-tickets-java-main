package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {
    private static final Logger LOGGER = Logger.getLogger(TicketServiceImpl.class.getName());

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {

        validateRequest(accountId, ticketTypeRequests);

        Map<Type, Long> ticketCounts = Arrays.stream(ticketTypeRequests)
                .collect(Collectors.groupingBy(TicketTypeRequest::getTicketType, Collectors.counting()));

        long adultCount = ticketCounts.getOrDefault(Type.ADULT, 0L);
        long childCount = ticketCounts.getOrDefault(Type.CHILD, 0L);

        if (adultCount == 0 && childCount > 0) {
            String errorMessage = "Child tickets cannot be purchased without purchasing an Adult ticket.";
            LOGGER.severe(errorMessage);
            throw new InvalidPurchaseException(errorMessage);
        }

        int totalPrice = (int) (adultCount * 20 + childCount * 10);
        int totalSeats = (int) (adultCount + childCount);

        processPayment(accountId, totalPrice);
        reserveSeats(accountId, totalSeats);
    }

    private void validateRequest(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests.length == 0 || ticketTypeRequests.length > 20) {
            String errorMessage = "Invalid number of tickets. Must purchase between 1 and 20 tickets.";
            LOGGER.severe(errorMessage);
            throw new InvalidPurchaseException(errorMessage);
        }

        Optional.ofNullable(accountId).filter(id -> id > 0)
                .orElseThrow(() -> {
                    String errorMessage = "Invalid Account ID.";
                    LOGGER.severe(errorMessage);
                    throw new InvalidPurchaseException(errorMessage);
                });
    }

    private void processPayment(Long accountId, int totalPrice) {
        new TicketPaymentServiceImpl().makePayment(accountId, totalPrice);
    }

    private void reserveSeats(Long accountId, int totalSeats) {
        new SeatReservationServiceImpl().reserveSeat(accountId, totalSeats);
    }
}
