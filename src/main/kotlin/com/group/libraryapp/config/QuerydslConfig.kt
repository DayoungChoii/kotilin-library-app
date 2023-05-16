package com.group.libraryapp.config

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManager

@Configuration
class QuerydslConfig(
    private val em: EntityManager
) {

    fun querydsl(): JPAQueryFactory {
        return JPAQueryFactory(em)
    }
}