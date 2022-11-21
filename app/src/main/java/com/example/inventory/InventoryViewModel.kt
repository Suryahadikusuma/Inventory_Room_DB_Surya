/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.launch

/**
 * View Model digunakan untuk menyimpan referensi ke repositori Inventaris dan juga daftar terbaru semua item.
 *
 */
class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    /**
     * Returns true if stock is available to sell, false otherwise.
     */
    fun isStockAvailable(item: Item): Boolean {
        return (item.quantityInStock > 0)
    }

    /**
     * Memperbarui Item yang ada di database.
     */
    fun updateItem(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String,
        itemDesc: String
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemName, itemPrice, itemCount, itemDesc)
        updateItem(updatedItem)
    }


    /**
     * Meluncurkan coroutine baru untuk memperbarui item
     */
    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    /**
     * Mengurangi stok sebanyak satu unit dan memperbarui database.
     */
    fun sellItem(item: Item) {
        if (item.quantityInStock > 0) {
            // Decrease the quantity by 1
            val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
            updateItem(newItem)
        }
    }

    /**
     * Untuk memasukan data baru ke databse
     */
    fun addNewItem(itemName: String, itemPrice: String, itemCount: String, itemDesc: String) {
        val newItem = getNewItemEntry(itemName, itemPrice, itemCount, itemDesc)
        insertItem(newItem)
    }

    /**
     * Meluncurkan coroutine baru untuk menyisipkan item
     */
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    /**
     * untuk mengambil item dari repositori.
     */
    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    /**
     * Berfungsi Mengembalikan nilai true jika EditTexts tidak kosong
     */
    fun isEntryValid(itemName: String, itemPrice: String, itemCount: String, itemDesc: String): Boolean {
        if (itemName.isBlank() || itemPrice.isBlank() || itemCount.isBlank() || itemDesc.isBlank()) {
            return false
        }
        return true
    }

    /**
     *  berfungasi Mengembalikan instance kelas entitas
     */
    private fun getNewItemEntry(itemName: String, itemPrice: String, itemCount: String, itemDesc: String): Item {
        return Item(
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt(),
            itemdescription = itemDesc
        )
    }

    /**
     * Dipanggil untuk memperbarui entri yang ada di database Inventaris.
     * Mengembalikan instance kelas entitas [Item] dengan info item yang diperbarui oleh pengguna.
     */
    private fun getUpdatedItemEntry(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String,
        itemDesc: String
    ): Item {
        return Item(
            id = itemId,
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt(),
            itemdescription = itemDesc
        )
    }
}

/**
 * Factory class untuk membuat instance [ViewModel].
 */
class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

