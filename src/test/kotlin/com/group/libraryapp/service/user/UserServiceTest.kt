package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
open class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    @Test
    fun saveUserTest() {
        //given
        val userCreateRequest = UserCreateRequest("다영", null)

        //when
        userService.saveUser(userCreateRequest)

        //then
        val users = userRepository.findAll()
        assertThat(users).hasSize(1)
        assertThat(users[0].name).isEqualTo("다영")
        assertThat(users[0].age).isNull()
    }

    @Test
    fun getUsersTest() {
        //given
        userRepository.saveAll(
            listOf(
                User("다영", 20),
                User("주영", null)
            )
        )

        //when
        val results = userService.getUsers();

        //then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("다영", "주영")
        assertThat(results).extracting("age").containsExactlyInAnyOrder(20, null)
    }

    @Test
    fun updateUserTest() {
        // given
        val user = User("다영", 20)
        userRepository.save(user)

        // when
        userService.updateUserName(UserUpdateRequest(user.id, "다영1"))

        // then
        val result = userRepository.findById(user.id)
        assertThat(result.get().name).isEqualTo("다영1")
    }

    @Test
    fun deleteUserTest() {
        // given
        val user = User("다영", null)
        userRepository.save(user)

        // when
        userService.deleteUser("다영")

        // then
        assertThat( userRepository.findAll()).isEmpty()
    }
}