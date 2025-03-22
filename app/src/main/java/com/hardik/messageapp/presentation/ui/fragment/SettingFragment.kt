package com.hardik.messageapp.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.hardik.messageapp.databinding.FragmentSettingBinding
import com.hardik.messageapp.helper.Constants.BASE_TAG
import com.hardik.messageapp.helper.LogUtil
import com.hardik.messageapp.helper.getConversations
import com.hardik.messageapp.helper.toJson
import com.hardik.messageapp.presentation.viewmodel.ContactViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private val TAG = BASE_TAG + SettingFragment::class.java.simpleName

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val contactViewModel: ContactViewModel by activityViewModels()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactViewModel.fetchContacts()
        lifecycleScope.launch {
//            contactViewModel.contacts.collectLatest {
//                it.forEach{ Log.i(TAG, "onViewCreated: $it") }
//                Log.v(TAG, "onViewCreated: ${it.size}")
//            }
                getConversations(requireContext()).collect { data ->
                    val json = data.toJson()
                    LogUtil.d(TAG, "onViewCreated: \n$json")
                }
        }

    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}