package com.example.petscue.util

import androidx.lifecycle.SavedStateHandle

import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.MensajesRepository
import com.example.petscue.data.repository.PostRepository
import com.example.petscue.data.repository.ProfileRepository
import com.example.petscue.data.repository.ReplyRepository
import com.example.petscue.ui.profile.ProfileTab
import com.example.petscue.ui.profile.ProfileViewModel
import com.example.petscue.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ProfileRepository = mock()
    private val mensajesRepository: MensajesRepository = mock()
    private val postRepository: PostRepository = mock()
    private val replyRepository: ReplyRepository = mock()

    @Test
    fun onTabSelected_actualizaSelectedTab() = runTest {
        val currentUser = User(
            uid = "user-1",
            nombre = "Ana",
            apellido = "López",
            username = "ana123",
            email = "ana@email.com",
            role = UserRole.USER
        )

        whenever(repository.getCurrentUserProfile()).thenReturn(currentUser)
        whenever(repository.getFollowersCount("user-1")).thenReturn(10)
        whenever(repository.getFollowingCount("user-1")).thenReturn(5)
        whenever(repository.observeFollowersCount("user-1")).thenReturn(flowOf(10))
        whenever(repository.observeFollowingCount("user-1")).thenReturn(flowOf(5))
        whenever(repository.getRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getRepostedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPetsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getAdoptionPetsByProtectora("user-1")).thenReturn(flowOf(emptyList()))

        val viewModel = ProfileViewModel(
            repository = repository,
            mensajesRepository = mensajesRepository,
            postRepository = postRepository,
            replyRepository = replyRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.onTabSelected(ProfileTab.LIKES)

        assertEquals(ProfileTab.LIKES, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun consumeOpenChatEvent_limpiaEvento() = runTest {
        val currentUser = User(
            uid = "user-1",
            nombre = "Ana",
            apellido = "López",
            username = "ana123",
            email = "ana@email.com",
            role = UserRole.USER
        )

        val viewedUser = User(
            uid = "user-2",
            nombre = "Luis",
            apellido = "Pérez",
            username = "luis123",
            email = "luis@email.com",
            role = UserRole.USER
        )

        whenever(repository.getCurrentUserProfile()).thenReturn(currentUser)
        whenever(repository.getUserProfileById("user-2")).thenReturn(viewedUser)
        whenever(repository.getFollowersCount("user-2")).thenReturn(7)
        whenever(repository.getFollowingCount("user-2")).thenReturn(3)
        whenever(repository.isFollowing("user-1", "user-2")).thenReturn(false)
        whenever(repository.observeFollowersCount("user-2")).thenReturn(flowOf(7))
        whenever(repository.observeFollowingCount("user-2")).thenReturn(flowOf(3))
        whenever(repository.getRepliesByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getRepostedPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPetsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getAdoptionPetsByProtectora("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(
            mensajesRepository.createOrGetGeneralConversation(
                currentUserId = "user-1",
                otherUserId = "user-2"
            )
        ).thenReturn("conv-1")

        val viewModel = ProfileViewModel(
            repository = repository,
            mensajesRepository = mensajesRepository,
            postRepository = postRepository,
            replyRepository = replyRepository,
            savedStateHandle = SavedStateHandle(mapOf("userId" to "user-2"))
        )
        advanceUntilIdle()

        viewModel.openOrCreateGeneralChat()
        advanceUntilIdle()

        assertEquals("conv-1", viewModel.openChatEvent.value)

        viewModel.consumeOpenChatEvent()

        assertNull(viewModel.openChatEvent.value)
    }

    @Test
    fun openOrCreateGeneralChat_siFalla_muestraError() = runTest {
        val currentUser = User(
            uid = "user-1",
            nombre = "Ana",
            apellido = "López",
            username = "ana123",
            email = "ana@email.com",
            role = UserRole.USER
        )

        val viewedUser = User(
            uid = "user-2",
            nombre = "Luis",
            apellido = "Pérez",
            username = "luis123",
            email = "luis@email.com",
            role = UserRole.USER
        )

        whenever(repository.getCurrentUserProfile()).thenReturn(currentUser)
        whenever(repository.getUserProfileById("user-2")).thenReturn(viewedUser)
        whenever(repository.getFollowersCount("user-2")).thenReturn(7)
        whenever(repository.getFollowingCount("user-2")).thenReturn(3)
        whenever(repository.isFollowing("user-1", "user-2")).thenReturn(false)
        whenever(repository.observeFollowersCount("user-2")).thenReturn(flowOf(7))
        whenever(repository.observeFollowingCount("user-2")).thenReturn(flowOf(3))
        whenever(repository.getRepliesByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getRepostedPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPetsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getAdoptionPetsByProtectora("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(
            mensajesRepository.createOrGetGeneralConversation(
                currentUserId = "user-1",
                otherUserId = "user-2"
            )
        ).thenThrow(RuntimeException("No se pudo abrir la conversación"))

        val viewModel = ProfileViewModel(
            repository = repository,
            mensajesRepository = mensajesRepository,
            postRepository = postRepository,
            replyRepository = replyRepository,
            savedStateHandle = SavedStateHandle(mapOf("userId" to "user-2"))
        )
        advanceUntilIdle()

        viewModel.openOrCreateGeneralChat()
        advanceUntilIdle()

        assertEquals("No se pudo abrir la conversación", viewModel.uiState.value.error)
        assertNull(viewModel.openChatEvent.value)
    }

    @Test
    fun toggleFollow_siNoSeguia_ahoraSigue() = runTest {
        val currentUser = User(
            uid = "user-1",
            nombre = "Ana",
            apellido = "López",
            username = "ana123",
            email = "ana@email.com",
            role = UserRole.USER
        )

        val viewedUser = User(
            uid = "user-2",
            nombre = "Luis",
            apellido = "Pérez",
            username = "luis123",
            email = "luis@email.com",
            role = UserRole.USER
        )

        whenever(repository.getCurrentUserProfile()).thenReturn(currentUser)
        whenever(repository.getUserProfileById("user-2")).thenReturn(viewedUser)
        whenever(repository.getFollowersCount("user-2")).thenReturn(7)
        whenever(repository.getFollowingCount("user-2")).thenReturn(3)
        whenever(repository.isFollowing("user-1", "user-2")).thenReturn(false)
        whenever(repository.observeFollowersCount("user-2")).thenReturn(flowOf(7))
        whenever(repository.observeFollowingCount("user-2")).thenReturn(flowOf(3))
        whenever(repository.getRepliesByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getRepostedPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPetsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getAdoptionPetsByProtectora("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))

        val viewModel = ProfileViewModel(
            repository = repository,
            mensajesRepository = mensajesRepository,
            postRepository = postRepository,
            replyRepository = replyRepository,
            savedStateHandle = SavedStateHandle(mapOf("userId" to "user-2"))
        )
        advanceUntilIdle()

        viewModel.toggleFollow()
        advanceUntilIdle()

        verify(repository).followUser(
            followerId = "user-1",
            followedId = "user-2"
        )
        assertTrue(viewModel.uiState.value.isFollowing)
    }

    @Test
    fun toggleFollow_siYaSeguia_haceUnfollow() = runTest {
        val currentUser = User(
            uid = "user-1",
            nombre = "Ana",
            apellido = "López",
            username = "ana123",
            email = "ana@email.com",
            role = UserRole.USER
        )

        val viewedUser = User(
            uid = "user-2",
            nombre = "Luis",
            apellido = "Pérez",
            username = "luis123",
            email = "luis@email.com",
            role = UserRole.USER
        )

        whenever(repository.getCurrentUserProfile()).thenReturn(currentUser)
        whenever(repository.getUserProfileById("user-2")).thenReturn(viewedUser)
        whenever(repository.getFollowersCount("user-2")).thenReturn(7)
        whenever(repository.getFollowingCount("user-2")).thenReturn(3)
        whenever(repository.isFollowing("user-1", "user-2")).thenReturn(true)
        whenever(repository.observeFollowersCount("user-2")).thenReturn(flowOf(7))
        whenever(repository.observeFollowingCount("user-2")).thenReturn(flowOf(3))
        whenever(repository.getRepliesByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getRepostedPostsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPetsByUser("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getAdoptionPetsByProtectora("user-2")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))

        val viewModel = ProfileViewModel(
            repository = repository,
            mensajesRepository = mensajesRepository,
            postRepository = postRepository,
            replyRepository = replyRepository,
            savedStateHandle = SavedStateHandle(mapOf("userId" to "user-2"))
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFollowing)

        viewModel.toggleFollow()
        advanceUntilIdle()

        verify(repository).unfollowUser(
            followerId = "user-1",
            followedId = "user-2"
        )
        assertFalse(viewModel.uiState.value.isFollowing)
    }

    @Test
    fun openOrCreateGeneralChat_siEsSuPropioPerfil_noHaceNada() = runTest {
        val currentUser = User(
            uid = "user-1",
            nombre = "Ana",
            apellido = "López",
            username = "ana123",
            email = "ana@email.com",
            role = UserRole.USER
        )

        whenever(repository.getCurrentUserProfile()).thenReturn(currentUser)
        whenever(repository.getFollowersCount("user-1")).thenReturn(10)
        whenever(repository.getFollowingCount("user-1")).thenReturn(5)
        whenever(repository.observeFollowersCount("user-1")).thenReturn(flowOf(10))
        whenever(repository.observeFollowingCount("user-1")).thenReturn(flowOf(5))
        whenever(repository.getRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getRepostedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedPostsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getLikedRepliesByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getPetsByUser("user-1")).thenReturn(flowOf(emptyList()))
        whenever(repository.getAdoptionPetsByProtectora("user-1")).thenReturn(flowOf(emptyList()))

        val viewModel = ProfileViewModel(
            repository = repository,
            mensajesRepository = mensajesRepository,
            postRepository = postRepository,
            replyRepository = replyRepository,
            savedStateHandle = SavedStateHandle()
        )
        advanceUntilIdle()

        viewModel.openOrCreateGeneralChat()
        advanceUntilIdle()

        assertNull(viewModel.openChatEvent.value)
        assertNull(viewModel.uiState.value.error)
    }
}