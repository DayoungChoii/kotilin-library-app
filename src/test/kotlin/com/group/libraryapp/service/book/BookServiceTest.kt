package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
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
    val book: Book = Book.fixture("연금술사", BookType.COMPUTER)

    @AfterEach
    fun tearDown() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
        userLoanHistoryRepository.deleteAll()
    }

    @Test
    fun saveBookTest() {
        // given
        val bookRequest = BookRequest("연금술사", BookType.COMPUTER)

        // when
        bookService.saveBook(bookRequest)

        // then
        val results = bookRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("연금술사")
        assertThat(results[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    fun loanBookSuccessTest() {
        // given
        bookRepository.save(book)
        val user = User("다영", null)
        userRepository.save(user)

        // when
        bookService.loanBook(BookLoanRequest("다영", "연금술사"))

        // then
        val result = userLoanHistoryRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].bookName).isEqualTo("연금술사")
        assertThat(result[0].user.id).isEqualTo(user.id)
        assertThat(result[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    fun loanBookFailTest() {
        // given
        bookRepository.save(book)
        val user = User("다영", null)
        userRepository.save(user)
        userLoanHistoryRepository.save(UserLoanHistory.fixture(user, "연금술사"))

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
        bookRepository.save(book)
        val user = User("다영", null)
        userRepository.save(user)
        userLoanHistoryRepository.save(UserLoanHistory.fixture(user, "연금술사"))

        // when
        bookService.returnBook(BookReturnRequest("다영", "연금술사"))

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    fun loanBookCountTest() {
        // given
        val user = userRepository.save(User("A", null))
        val loanHistory1 = UserLoanHistory.fixture(user = user, status = UserLoanStatus.RETURNED)
        val loanHistory2 = UserLoanHistory.fixture(user = user, status = UserLoanStatus.RETURNED)
        val loanHistory3 = UserLoanHistory.fixture(user = user, bookName = "b")
        userLoanHistoryRepository.save(loanHistory1)
        userLoanHistoryRepository.save(loanHistory2)
        userLoanHistoryRepository.save(loanHistory3)

        // when
        val result = bookService.countLoanBook()

        // then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(Book.fixture("a", BookType.COMPUTER),
            Book.fixture("b", BookType.COMPUTER),
            Book.fixture("c", BookType.ECONOMY)
        ))

        // when
        val results = bookService.getBookStatistics()


        // then
        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2L)
        assertCount(results, BookType.ECONOMY, 1L)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
        assertThat(results.first() { dto -> dto.type == type }.count).isEqualTo(count)
    }


}