package com.d10s3.test.contactssearch

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d10s3.test.contactssearch.databinding.ActivityMainBinding
import com.d10s3.test.contactssearch.databinding.ItemContactBinding
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val permission = Permissions(this)
    private lateinit var contactAdapter: ContactAdapter
    private var isSearch: Boolean = false
    private var choSungMap: HashMap<String?, String?> = object : HashMap<String?, String?>() {
        init {
            put("ㄱ", "0")
            put("ㄲ", "1")
            put("ㄴ", "2")
            put("ㄷ", "3")
            put("ㄸ", "4")
            put("ㄹ", "5")
            put("ㅁ", "6")
            put("ㅂ", "7")
            put("ㅃ", "8")
            put("ㅅ", "9")
            put("ㅆ", "10")
            put("ㅇ", "11")
            put("ㅈ", "12")
            put("ㅉ", "13")
            put("ㅊ", "14")
            put("ㅋ", "15")
            put("ㅌ", "16")
            put("ㅍ", "17")
            put("ㅎ", "18")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permission.check(Manifest.permission.READ_CONTACTS, object : Permissions.OnPermissionCheckCallback {
            override fun onPermissionResult(result: Boolean) {
                if (result) {
                    var contactRepository = ContactRepository(baseContext)
                    var contact = contactRepository.fetchContacts()
                    contactAdapter = ContactAdapter(contact, contact)
                    binding.recyclerView.layoutManager = LinearLayoutManager(baseContext)
                    binding.recyclerView.adapter = contactAdapter
                    binding.recyclerView.setEmptyView(binding.emptyTextView)

                    binding.evSearch.setOnClickListener {
                        isSearch = true
                        binding.layoutSearch.visibility = View.INVISIBLE
                        binding.svSearch.visibility = View.VISIBLE
                        setUpSearch()
                    }
                } else {
                    // not permission
                }
            }
        })
    }

    private fun setUpSearch() {
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (contactAdapter != null) {
                    contactAdapter.getFilter().filter(newText)
                }
                return true
            }
        })

        binding.svSearch.setOnCloseListener(object : SearchView.OnCloseListener,
            androidx.appcompat.widget.SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                closeSearch()
                return true
            }
        })
    }

    private fun closeSearch() {
        if (isSearch) {
            isSearch = false
            binding.layoutSearch.visibility = View.VISIBLE
            binding.svSearch.visibility = View.INVISIBLE
        }
    }

    private fun isNumber(value: String): Boolean {
        return value.replace("[+-]?\\d+".toRegex(), "") == ""
    }

    private fun isHangul(value: String): Boolean {
        val regex = "[ㄱ-ㅎ|가-힣]+".toRegex()
        return regex.matches(value)
    }

    private fun split(c: Char): IntArray {
        val sub = IntArray(3)
        sub[0] = (c.code - 0xAC00) / (21 * 28) //초성의 위치
        sub[1] = (c.code - 0xAC00) % (21 * 28) / 28 //중성의 위치
        sub[2] = (c.code - 0xAC00) % 28 //종성의 위치
        return sub
    }

    private fun getChosung(c: Char): String? {
        return choSungMap[c.toString()]
    }

    private fun getHangulArray(str: String): String {
        var retStr: String = ""
        val loop = str.length
        var x: IntArray
        var c: Char
        for (i in 0 until loop) {
            c = str[i]
            if (c.code >= 0xAC00) {
                x = split(c)
                if (i >= 1) retStr += "_"
                retStr += if (x[2] == 0) x[0].toString() + "/" + x[1] else x[0].toString() + "/" + x[1] + "/" + x[2]
            } else {
                if (i >= 1) retStr += "_"
                val tempStr: String? = getChosung(c)
                retStr += tempStr
            }
        }
        return retStr
    }

    inner class ContactAdapter(private var contacts: List<Contact>, private var filteredContact: MutableList<Contact>) :
        RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

        private var mFilter: ItemFilter = ItemFilter()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
            return ViewHolder(ItemContactBinding.bind(view))
        }

        override fun getItemCount(): Int = filteredContact.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.tvContactName.text = filteredContact[position].name
        }

        fun setContacts(contacts: MutableList<Contact>) {
            this.contacts = contacts
            this.filteredContact = contacts
        }

        fun getFilter(): ItemFilter {
            return mFilter
        }

        inner class ViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.layoutContact.setOnClickListener {
                    onItemClick(adapterPosition)
                }
            }
        }

        private fun onItemClick(adapterPosition: Int) {
            Toast.makeText(baseContext, filteredContact[adapterPosition].name, Toast.LENGTH_SHORT).show()
        }

        inner class ItemFilter: Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var results: FilterResults = FilterResults()
                var list: List<Contact> = contacts
                var resultList: MutableList<Contact> = ArrayList(list.size)
                var query: String = String()

                if (isNumber(constraint.toString())) {
                    query = constraint.toString().lowercase(Locale.getDefault())
                    for (i: Int in list.indices) {
                        var name = list[i].phoneNumber
                        if (name.lowercase(Locale.getDefault()).contains(query)) {
                            resultList.add(list[i])
                        }
                    }
                    results.values = resultList
                    results.count = resultList.size
                } else if (isHangul(constraint.toString())) {
                    var divStr = getHangulArray(constraint.toString())
                    var divArray = divStr?.split("_").toTypedArray()
                    var subDivArray = Array<Array<String>>(divArray.size) { emptyArray() }

                    for (i: Int in divArray.indices) {
                        subDivArray[i] = divArray[i].split("/").toTypedArray()
                    }

                    var divFlag = -1 // -1 초기 / 0 진행중 / 1 일치

                    for (index: Int in list.indices) {
                        var name = list[index].name
                        divFlag = -1

                        var divStr2 = getHangulArray(name)
                        var divArray2 = divStr2.split("_").toTypedArray()
                        var subDivArray2 = Array<Array<String>>(divArray2.size) { emptyArray() }

                        if (divArray.size > divArray2.size) {
                            divFlag = -1
                            continue
                        }
                        var i = 0
                        for (j: Int in divArray2.indices) {
                            if (divFlag == 1) break

                            subDivArray2[j] = divArray2[j].split("/").toTypedArray()
                            if (subDivArray[i].size > subDivArray2[j].size) {
                                if (divFlag == 0) {
                                    divFlag = -1
                                    i = 0
                                }
                                continue
                            } else {
                                var subDivFlag = -1
                                for (k: Int in subDivArray[i].indices) {
                                    if (subDivArray[i][k] != subDivArray2[j][k]) {
                                        subDivFlag = 0
                                        break
                                    } else {
                                        if (subDivFlag == -1) {
                                            subDivFlag = 1
                                        }
                                    }
                                }
                                if (subDivFlag == 1) {
                                    if (divFlag == -1) {
                                        divFlag = if (divArray.size == 1) 1 else 0
                                    } else if (divFlag == 0){
                                        if (divArray.size - 1 == i)
                                            divFlag = 1
                                    }

                                    i++
                                }
                            }
                        }
                        if (divFlag == 1)
                            resultList.add(list[index])
                    }
                    results.values = resultList
                    results.count = resultList.size
                } else {
                    query = constraint.toString().lowercase(Locale.getDefault())
                    for (i: Int in list.indices) {
                        var name = list[i].name
                        if (name.lowercase(Locale.getDefault()).contains(query)) {
                            resultList.add(list[i])
                        }
                    }
                    results.values = resultList
                    results.count = resultList.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.values != null) {
                    filteredContact = results.values as MutableList<Contact>
                    notifyDataSetChanged()
                }
            }
        }
    }
}