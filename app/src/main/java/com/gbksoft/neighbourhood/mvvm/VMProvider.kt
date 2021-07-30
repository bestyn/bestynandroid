package com.gbksoft.neighbourhood.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class VMProvider<VM : ViewModel> : ViewModelProvider {
    private val vmClass: Class<VM>

    constructor(owner: ViewModelStoreOwner, factory: Factory, vmClass: Class<VM>) : super(owner, factory) {
        this.vmClass = vmClass
    }

    constructor(store: ViewModelStore, factory: Factory, vmClass: Class<VM>) : super(store, factory) {
        this.vmClass = vmClass
    }


    companion object {
        inline fun <reified VM : ViewModel> create(owner: ViewModelStoreOwner, noinline viewModelCreator: () -> VM)
            : VMProvider<VM> {
            return VMProvider(owner, VMFactory(viewModelCreator), VM::class.java)
        }

        inline fun <reified VM : ViewModel> create(store: ViewModelStore, noinline viewModelCreator: () -> VM)
            : VMProvider<VM> {
            return VMProvider(store, VMFactory(viewModelCreator), VM::class.java)
        }
    }

    fun get(): VM {
        return get(vmClass)
    }

    class VMFactory<VM : ViewModel>(private val viewModelCreator: () -> VM) : Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return viewModelCreator.invoke() as T
        }

    }
}