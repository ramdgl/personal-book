package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.demo.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookControllerTests {

    private MockMvc mockMvc;

    @Mock
    private BookRepository bookRepository;

    
    @Mock
    private BookService bookService;

    @InjectMocks
    private com.example.demo.BookController bookController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

        // seed repository mock for GET test
        Book b1 = new Book("lRtdEAAAQBAJ", "Spring in Action", "Craig Walls");
        Book b2 = new Book("12muzgEACAAJ", "Effective Java", "Joshua Bloch");
        when(bookRepository.findAll()).thenReturn(List.of(b1, b2));
    }

    @Test
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/books").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring in Action"))
                .andExpect(jsonPath("$[1].title").value("Effective Java"));
    }

    @Test
    void testPostBook() throws Exception {
        Book expected = new Book("dvCnEAAAQBAJ", "Robotic Process Automation (RPA) - Digitization and Automation of Processes", "Christian Langmann");
        expected.setPageCount(128);
        when(bookService.saveBook(anyString())).thenReturn(expected);

        mockMvc.perform(post("/books/{googleId}", "dvCnEAAAQBAJ").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("dvCnEAAAQBAJ"))
                .andExpect(jsonPath("$.title").value("Robotic Process Automation (RPA) - Digitization and Automation of Processes"))
                .andExpect(jsonPath("$.author").value("Christian Langmann"))
                .andExpect(jsonPath("$.pageCount").value(128));
    }

}
