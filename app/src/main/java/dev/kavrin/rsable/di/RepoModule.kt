package dev.kavrin.rsable.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dev.kavrin.rsable.data.local.BleGattManager
import dev.kavrin.rsable.data.local.BleGattManagerImpl
import dev.kavrin.rsable.data.local.BleScanManager
import dev.kavrin.rsable.data.local.BleScanManagerImpl
import dev.kavrin.rsable.data.repository.BleRepositoryImpl
import dev.kavrin.rsable.domain.repository.BleRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val repoModule = module {
    single { (get<Context>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter }.bind(
        BluetoothAdapter::class
    )
    single { BleScanManagerImpl(bleAdapter = get()) }.bind(BleScanManager::class)
    single { BleGattManagerImpl(context = get()) }.bind(BleGattManager::class)
    single {
        BleRepositoryImpl(
            bleGattManager = get(),
            bleScanManager = get()
        )
    }.bind(BleRepository::class)
}
