package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.PortDao
import com.cyberlogitec.freight9.lib.db.PortDao.PortMinimal
import com.cyberlogitec.freight9.lib.model.Port
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.texy.treeview.TreeNode
import timber.log.Timber
import java.util.*

class PortRepository(val portDao: PortDao) : PortRepositoryType {
    lateinit var baseTreeNode: TreeNode

    override fun initializeBaseTree() {
        val root = TreeNode.root()
        val continents = portDao.getContinents()
        continents.map { continent->
            val continentNode = TreeNode(continent).apply { level = 0 }
            val countries = portDao.getCountries(continent)
            countries.map { country->
                val countryNode = TreeNode(country).apply { level = 1 }
                val ports = portDao.getPorts(country)
                ports.map { port->
                    val portNode = TreeNode(port).apply { level = 2 }
                    countryNode.addChild(portNode)
                }
                continentNode.addChild(countryNode)
            }
            root.addChild(continentNode)
        }
        baseTreeNode = root
    }

    override fun loadAllPorts(): Single<List<Port>> =
            portDao.loadAllPorts()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun loadTestRoute(): Single<List<PortDao.RouteMinimal>> =
            portDao.loadTestRoute()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun customCapitalize(name: String) : String{
        val sb = StringBuilder()
        name.split(" ").map { block->
            sb.append(" "+block.toLowerCase().capitalize())
        }
        val capitalized = sb.toString().trim()
        sb.clear()
        if (capitalized.contains(',')){
            val forwardChunk = capitalized.substring(0, capitalized.indexOf(','))
            val backwardChunk = capitalized.substring(capitalized.indexOf(',')).toUpperCase()
            sb.append(forwardChunk)
            sb.append(backwardChunk)
        } else {
            sb.append(capitalized)
        }
        return sb.toString().trim()
    }

    override fun insertPortData(ports: List<Port>) {
        Observable.fromCallable { portDao.insertAll(ports) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${ports.size} ports from API in DB...")
                }
    }

    override fun searchPort(value: String): Observable<List<PortMinimal>> {
        return portDao.searchPort(value+"%")
    }

    override fun isInland(portCode: String): Boolean {
        return portDao.getIsInland(portCode)
    }

    override fun getRecentPorts(): Observable<List<PortMinimal>> {
        return Observable.fromCallable { portDao.getRecentPorts() }
    }

    override fun getPortNm(portCode: String): String {
        return portDao.getPortNm(portCode)
    }

    override fun updateSelectedPortDate(portCode: String) {
        Observable.fromCallable { portDao.updatePortSelectedDate(portCode, Date()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: update port selected date at ${Date()}")
                }
    }

    override fun deleteAll() {
        Observable.fromCallable { portDao.deleteAll() }
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Timber.d("diver:/ delete all ports")
                }
    }

    override fun hasPortData(): Boolean {
        return (portDao.getPortCount() > 0)
//        return Single.fromCallable { portDao.loadAllPorts().size > 0 }
    }

    override fun isPortValid(): Observable<Boolean> {
        return Observable.fromCallable { (portDao.getPortCount() > 0) }
    }



    override fun makeBaseTree(): TreeNode {
        val root = TreeNode.root()
        val continents = portDao.getContinents()
        continents.map { continent->
            val continentNode = TreeNode(continent).apply { level = 0 }
            val countries = portDao.getCountries(continent)
            countries.map { country->
                val countryNode = TreeNode(country).apply { level = 1 }
                val ports = portDao.getPorts(country)
                ports.map { port->
                    val portNode = TreeNode(port).apply { level = 2 }
                    countryNode.addChild(portNode)
                }
                continentNode.addChild(countryNode)
            }
            root.addChild(continentNode)
        }
        return root
    }

    override fun makeTree(): TreeNode {
        Timber.v("diver:/ call loadPortTree() at PortRepository")
        val root = TreeNode.root()
        val recentPorts = portDao.getRecentPorts()
        val recent = TreeNode("Recently")
        recentPorts.map { recent.addChild(TreeNode(it).apply { level = 2}) }
        root.addChild(recent)
        baseTreeNode.children.map { root.addChild(it) }
        return root
    }

    override fun loadPortTree(): TreeNode {
        Timber.v("diver:/ call loadPortTree() at PortRepository")
        val root = TreeNode.root()
        val recentPorts = portDao.getRecentPorts()
        val recent = TreeNode("Recent")
        recentPorts.map {
            recent.addChild(TreeNode(it).apply { level = 2})
        }
        root.addChild(recent)
        Timber.v("diver:/ building Port Tree")
        val continents = portDao.getContinents()
        continents.map { continent->
            val continentNode = TreeNode(continent).apply { level = 0 }
            val countries = portDao.getCountries(continent)
            countries.map { country->
                val countryNode = TreeNode(country).apply { level = 1 }
                val ports = portDao.getPorts(country)
                ports.map { port->
                    val portNode = TreeNode(port).apply { level = 2 }
                    countryNode.addChild(portNode)
                }
                continentNode.addChild(countryNode)
            }
            root.addChild(continentNode)
        }
        Timber.v("diver:/ Port Tree build Done!")
        return root
    }
}