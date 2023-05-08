package com.group.libraryapp.dto.book.response

import com.group.libraryapp.domain.book.BookType

data class BookStatResponse (
    var count: Int,
    val type: BookType,
){
    fun plusOne() = this.count++
}
