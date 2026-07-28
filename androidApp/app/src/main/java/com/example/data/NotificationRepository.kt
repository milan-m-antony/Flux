package com.example.data

import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun insert(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun insertAll(notifications: List<NotificationEntity>) {
        notificationDao.insertAll(notifications)
    }

    suspend fun markAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun delete(id: Int) {
        notificationDao.deleteNotification(id)
    }

    suspend fun deleteAll() {
        notificationDao.deleteAll()
    }
}
