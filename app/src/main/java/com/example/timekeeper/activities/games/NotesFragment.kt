package com.example.timekeeper.activities.games

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.timekeeper.R
import com.example.timekeeper.activities.lock.LockScreenActivity
import com.example.timekeeper.database.DBHandler
import com.example.timekeeper.databinding.FragmentNotesBinding
import com.example.timekeeper.viewmodel.NotesViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private var _viewModel: NotesViewModel? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel

    private val parentIsLock get() = requireActivity().javaClass == LockScreenActivity::class.java

    private val logTag = "NotesFragment"

    private val arrayAdapter by lazy {
        ArrayAdapter(
            requireContext(),
            R.layout.note_list_item,
            R.id.mat_btn_note,
            viewModel!!.getNotes()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        if(parentIsLock) _viewModel!!.apply {
            dbHandler = DBHandler(requireContext())
            readNotes()
        } else _viewModel!!.updateDB = false

        binding.apply {
            notesList.adapter = arrayAdapter
            notesList.onItemClickListener = OnItemClickListener { arrayView, _, pos, _ ->
                val txt = arrayView.getItemAtPosition(pos) as String
                createAlertDialog(txt, pos)
            }

            fabAddNote.setOnClickListener {
                createAlertDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _viewModel!!.emptyNotes()
        Log.d(logTag, "Fragment destroyed")
    }

    private fun createAlertDialog(txt: String = "", pos: Int = -1) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Note")

        // Set up the input
        val editText = EditText(requireContext())

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        editText.setText(txt)
        builder.setView(editText)

        // Set up the buttons
        builder.setPositiveButton("OK") { _, _ ->
            editText.text.toString().let {
                if (it.isNotEmpty()) {
                    if (pos == -1) viewModel!!.addNote(it)
                    else viewModel!!.setNote(pos,it)
                    arrayAdapter.notifyDataSetChanged()
                    binding.textCreateFirstMemo.isVisible = arrayAdapter.isEmpty
                    showSuccessAndQuit()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showSuccessAndQuit() {
        Snackbar.make(
            binding.notesCoordinatorLayout,
            "Task is completed",
            Snackbar.LENGTH_LONG
        ).setAction("Action", null).show()
        if(parentIsLock) Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().finishAffinity()
        }, 3000)
    }
}