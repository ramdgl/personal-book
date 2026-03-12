package com.example.demo.service;

import com.example.demo.db.BookRepository;
import com.example.demo.db.Book;
import com.example.demo.dto.BookDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BookService {

    private final RestClient restClient;
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository,
                      @Value("${google.books.base-url:https://www.googleapis.com/books/v1}") String baseUrl) {
        this.bookRepository = bookRepository;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Book saveBook(String googleId) {
        BookDTO bookDTO = extractBookFields(googleId);
        System.out.println("BookDTO: " + bookDTO);
        if (bookDTO == null || bookDTO.id() == null || bookDTO.title() == null || bookDTO.author() == null) {
            System.err.println("Error: Missing required book fields. Book not saved.");
            return null;
        }
        Book book = new Book(
            bookDTO.id(),
            bookDTO.title(),
            bookDTO.author(),
            bookDTO.pageCount()
        );

        System.out.println("Book saved in h2 db");

        return bookRepository.save(book);
    }

    public BookDTO extractBookFields(String googleId) {
        String uri = "/volumes/" + googleId;
        String requestUrl = "https://www.googleapis.com/books/v1/volumes/" + googleId;
        System.out.println("Request URL: " + requestUrl);

        String jsonResponse = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

//        System.out.println("JSON Response: " + jsonResponse);

        BookDTO dto = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            String id = root.path("id").asText(null);
            JsonNode volumeInfo = root.path("volumeInfo");
            String title = volumeInfo.path("title").asText(null);
            JsonNode authors = volumeInfo.path("authors");
            String author = null;
            if (authors.isArray()) {
                if (!authors.isEmpty()) {
                    author = authors.get(0).asText(null);
                }
            } else if (authors.isTextual()) {
                author = authors.asText();
            }
            Integer pageCount = volumeInfo.has("pageCount") ? volumeInfo.path("pageCount").asInt() : null;
            dto = new BookDTO(id, title, author, pageCount);
        } catch (Exception e) {
            System.err.println("Error parsing book JSON: " + e.getMessage());
        }
        return dto;
    }
}
