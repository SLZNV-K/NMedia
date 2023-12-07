package ru.netology.nmedia.repository

import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post

class PostRepositorySQLiteImpl(
    private val dao: PostDao
) : PostRepository {

    override fun getAll(): List<Post> {
        TODO("Not yet implemented")
    }

    override fun likeById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override fun dislikeById(id: Long): Post {
        TODO("Not yet implemented")
    }
//    override fun get(): LiveData<List<Post>> = dao.getAll().map { posts ->
//        posts.map {
//            it.toDto()
//        }
//    }

//    override fun save(post: Post) {
//        dao.save(PostEntity.fromDto(post))
//    }

//    override fun likeById(id: Long) {
//        dao.likeById(id)
//
//    }

    override fun shareById(id: Long) {
        dao.shareById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun save(post: Post): Post {
        TODO("Not yet implemented")
    }
}
