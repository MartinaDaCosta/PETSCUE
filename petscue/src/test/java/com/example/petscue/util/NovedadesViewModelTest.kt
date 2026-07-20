package com.example.petscue.util

import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.PostRepository
import com.example.petscue.data.repository.ReplyRepository
import com.example.petscue.domain.usecase.DeletePostUseCase
import com.example.petscue.domain.usecase.GetPostsUseCase
import com.example.petscue.domain.usecase.InsertPostUseCase
import com.example.petscue.ui.novedades.NovedadesViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NovedadesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPostsUseCase: GetPostsUseCase = mock()
    private val insertPostUseCase: InsertPostUseCase = mock()
    private val deletePostUseCase: DeletePostUseCase = mock()
    private val postRepository: PostRepository = mock()
    private val replyRepository: ReplyRepository = mock()
    private val auth: FirebaseAuth = mock()
    private val db: FirebaseFirestore = mock()

    private fun createUser() = User(
        uid = "user-1",
        nombre = "Ana",
        apellido = "López",
        username = "ana123"
    )

    private fun createPost(
        id: String = "post-1",
        likedBy: List<String> = emptyList(),
        repostedBy: List<String> = emptyList(),
        sharedBy: List<String> = emptyList(),
        likes: Int = likedBy.size
    ) = Post(
        id = id,
        likedBy = likedBy,
        repostedBy = repostedBy,
        sharedBy = sharedBy,
        likes = likes
    )

    private fun mockCurrentUserLoaded(user: User = createUser()) {
        val firebaseUser: FirebaseUser = mock()
        val usersCollection: CollectionReference = mock()
        val userDocument: DocumentReference = mock()
        val userSnapshot: DocumentSnapshot = mock()

        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn(user.uid)

        whenever(db.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document(user.uid)).thenReturn(userDocument)
        whenever(userDocument.get()).thenReturn(Tasks.forResult(userSnapshot))
        whenever(userSnapshot.toObject(User::class.java)).thenReturn(user)
    }
    @Test
    fun init_cargaCurrentUserCorrectamente() = runTest {
        val user = createUser()
        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(emptyList()))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        assertEquals("user-1", viewModel.uiState.value.currentUser.uid)
    }
    @Test
    fun init_cargaUsuarioYPosts() = runTest {
        val user = createUser()
        val posts = listOf(createPost())

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(posts))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("user-1", state.currentUser.uid)
        assertEquals(1, state.posts.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun loadPosts_siFalla_muestraError() = runTest {
        mockCurrentUserLoaded()
        whenever(getPostsUseCase()).thenReturn(
            flow {
                throw RuntimeException("Error cargando publicaciones")
            }
        )

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Error cargando publicaciones", viewModel.uiState.value.error)
    }

    @Test
    fun insertPost_siFalla_muestraError() = runTest {
        mockCurrentUserLoaded()
        whenever(getPostsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(insertPostUseCase(any(), any())).thenThrow(RuntimeException("No se pudo publicar"))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.insertPost(createPost(), emptyList())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("No se pudo publicar", viewModel.uiState.value.error)
    }

    @Test
    fun deletePost_siFalla_muestraError() = runTest {
        val post = createPost()

        mockCurrentUserLoaded()
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))
        whenever(deletePostUseCase(post)).thenThrow(RuntimeException("No se pudo borrar la publicación"))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.deletePost(post)
        advanceUntilIdle()

        assertEquals("No se pudo borrar la publicación", viewModel.uiState.value.error)
    }

    @Test
    fun toggleLike_sinUsuario_muestraError() = runTest {
        val post = createPost()

        whenever(auth.currentUser).thenReturn(null)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.toggleLike(post)

        assertEquals(
            "Debes iniciar sesión para realizar esta acción",
            viewModel.uiState.value.error
        )
    }

    @Test
    fun toggleLike_actualizaEstadoLocal() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1", likedBy = emptyList(), likes = 0)

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))


        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.toggleLike(viewModel.uiState.value.posts.first())
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        verify(postRepository).toggleLike("post-1", "user-1")
    }
    @Test
    fun toggleLike_siFalla_revierteCambiosYMuestraError() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1", likedBy = emptyList(), likes = 0)

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))
        whenever(postRepository.toggleLike("post-1", "user-1"))
            .thenThrow(RuntimeException("No se pudo actualizar la publicación"))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.toggleLike(post)
        advanceUntilIdle()

        val revertedPost = viewModel.uiState.value.posts.first()
        assertFalse(revertedPost.likedBy.contains("user-1"))
        assertEquals(0, revertedPost.likes)
        assertEquals("No se pudo actualizar la publicación", viewModel.uiState.value.error)
    }

    @Test
    fun toggleRepost_actualizaEstadoLocal() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1", repostedBy = emptyList())

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))


        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.toggleRepost(viewModel.uiState.value.posts.first())
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        verify(postRepository).toggleRepost("post-1", "user-1")
    }

    @Test
    fun sharePost_actualizaEstadoLocal() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1", sharedBy = emptyList())

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.sharePost(viewModel.uiState.value.posts.first())
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        verify(postRepository).toggleShare("post-1", "user-1")
    }

    @Test
    fun addComment_sinUsuario_muestraError() = runTest {
        val post = createPost(id = "post-1")

        whenever(auth.currentUser).thenReturn(null)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.addComment(post, "Comentario")
        advanceUntilIdle()

        assertEquals("Debes iniciar sesión para comentar", viewModel.uiState.value.error)
    }

    @Test
    fun addComment_siTodoVaBien_insertaReplyYActualizaComentarios() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1")

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))

        val postsCollection: CollectionReference = mock()
        val postDocument: DocumentReference = mock()
        whenever(db.collection("posts")).thenReturn(postsCollection)
        whenever(postsCollection.document("post-1")).thenReturn(postDocument)
        whenever(postDocument.update(eq("comentarios"), any()))
            .thenReturn(Tasks.forResult(null))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.addComment(post, "Comentario de prueba")
        advanceUntilIdle()

        verify(replyRepository).insertReply(eq("post-1"), any())
        verify(postDocument).update(eq("comentarios"), any())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun addComment_siTextoVacio_noHaceNada() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1")

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.addComment(post, "   ")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun toggleSave_guardaPost() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1")

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.toggleSave(post)

        assertTrue(viewModel.uiState.value.savedPostIds.contains("post-1"))
    }

    @Test
    fun toggleSave_siYaExiste_loQuita() = runTest {
        val user = createUser()
        val post = createPost(id = "post-1")

        mockCurrentUserLoaded(user)
        whenever(getPostsUseCase()).thenReturn(flowOf(listOf(post)))

        val viewModel = NovedadesViewModel(
            getPostsUseCase = getPostsUseCase,
            insertPostUseCase = insertPostUseCase,
            deletePostUseCase = deletePostUseCase,
            postRepository = postRepository,
            replyRepository = replyRepository,
            auth = auth,
            db = db
        )
        advanceUntilIdle()

        viewModel.toggleSave(post)
        viewModel.toggleSave(post)

        assertFalse(viewModel.uiState.value.savedPostIds.contains("post-1"))
    }
}