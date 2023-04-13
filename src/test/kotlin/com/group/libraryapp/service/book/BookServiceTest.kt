package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookRepository: BookRepository,
    private val bookService: BookService,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
){

    @AfterEach
    fun tearDown() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
        userLoanHistoryRepository.deleteAll()
    }

    @Test
    fun saveBookTest() {
        // given
        val bookRequest = BookRequest("연금술사")

        // when
        bookService.saveBook(bookRequest)

        // then
        val results = bookRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("연금술사")
    }

    @Test
    fun loanBookSuccessTest() {
        // given
        bookRepository.save(Book("연금술사"))
        val user = User("다영", null)
        userRepository.save(user)

        // when
        bookService.loanBook(BookLoanRequest("다영", "연금술사"))

        // then
        val result = userLoanHistoryRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].bookName).isEqualTo("연금술사")
        assertThat(result[0].user.id).isEqualTo(user.id)
        assertThat(result[0].isReturn).isFalse
    }

    @Test
    fun loanBookFailTest() {
        // given
        bookRepository.save(Book("연금술사"))
        val user = User("다영", null)
        userRepository.save(user)
        userLoanHistoryRepository.save(UserLoanHistory(user, "연금술사", false))

        // when & then
        val result = assertThrows<IllegalArgumentException> {
            bookService.loanBook(BookLoanRequest("다영", "연금술사"))
        }.message
        result

        assertThat(result).isEqualTo("이미 대출되어 있는 책입니다")
    }

    @Test
    fun returnBookTest() {
        // given
        bookRepository.save(Book("연금술사"))
        val user = User("다영", null)
        userRepository.save(user)
        userLoanHistoryRepository.saveAndFlush(UserLoanHistory(user, "연금술사", false))

        // when
        bookService.returnBook(BookReturnRequest("다영", "연금술사"))

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].isReturn).isTrue
    }


}