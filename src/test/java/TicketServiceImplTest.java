import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImplTest {

    private TicketServiceImpl ticketService;

    @BeforeEach
    public void setUp() {
        ticketService = new TicketServiceImpl();
    }

    @Test
    public void purchaseTickets_ValidRequest_Success() {
        TicketTypeRequest[] requests = new TicketTypeRequest[] {
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
            new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)
        };

        Assertions.assertDoesNotThrow(() -> ticketService.purchaseTickets(1L, requests));
    }

    @Test
    public void purchaseTickets_InvalidAccountId_ExceptionThrown() {
        TicketTypeRequest[] requests = new TicketTypeRequest[] {
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        };

        Assertions.assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(0L, requests));
    }

    @Test
    public void purchaseTickets_TooManyTickets_ExceptionThrown() {
        TicketTypeRequest[] requests = new TicketTypeRequest[21];
        for (int i = 0; i < 21; i++) {
            requests[i] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        }

        Assertions.assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L, requests));
    }

}
