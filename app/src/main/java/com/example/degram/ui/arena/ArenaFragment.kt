package com.example.degram.ui.arena

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.degram.R
import com.example.degram.data.Arena
import com.example.degram.databinding.FragmentArenaBinding
import com.example.degram.databinding.LayoutJoinArenasBinding
import com.example.degram.util.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ArenaFragment : Fragment() {

    private val viewModel: ArenaViewModel by viewModels()
    private lateinit var binding: FragmentArenaBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_arena, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = ArenaMemberAdapter()
        binding.membersList.adapter = adapter

        viewModel.authenticateUser.observe(viewLifecycleOwner, { if (it == true) logInUser() })

        viewModel.showAddArenaLayout.observe(viewLifecycleOwner, { if (it) showAddArenaLayout() })

        viewModel.toast.observe(viewLifecycleOwner, { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() })

        viewModel.arenas.observe(viewLifecycleOwner, { list ->
            val arena = list[viewModel.arenaIndex.value!!]
            binding.arena = arena
            adapter.submitList(arena.members.filter { arena.membersUid.contains(it.uid) }.toMutableList())
        })

        viewModel.arenaIndex.observe(viewLifecycleOwner, { index ->
            if (!viewModel.arenas.value.isNullOrEmpty()) {
                val arena = viewModel.arenas.value!![index]
                binding.arena = arena
                adapter.submitList(arena.members.toMutableList())
            }
        })

        binding.inviteBtn.setOnClickListener { invite(viewModel.arenas.value!![viewModel.arenaIndex.value!!]) }
        binding.leaveBtn.setOnClickListener { leave(viewModel.arenas.value!![viewModel.arenaIndex.value!!]) }
        binding.deleteBtn.setOnClickListener { delete(viewModel.arenas.value!![viewModel.arenaIndex.value!!]) }

        binding.signInButton.setOnClickListener { viewModel.startAuthentication() }

        return binding.root
    }

    private fun showAddArenaLayout() {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.bottomSheetDialog)
        val bindingSheet = DataBindingUtil.inflate<LayoutJoinArenasBinding>(
                layoutInflater,
                R.layout.layout_join_arenas,
                null,
                false
        )
        bindingSheet.viewModel = viewModel
        bindingSheet.lifecycleOwner = this
        viewModel.dismissSheet.observe(viewLifecycleOwner, { if (it) bottomSheet.dismiss() })
        bottomSheet.setContentView(bindingSheet.root)
        bottomSheet.show()
    }

    private fun logInUser() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val signInIntent = mGoogleSignInClient.signInIntent
        signInActivity.launch(signInIntent)
    }

    private val signInActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun invite(arena: Arena?) {
        val intent = Intent(Intent.ACTION_SEND)
        val shareBody = getString(R.string.invite_member, arena?.code, Constants.APP_DOWNLOAD_LINK)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    private fun delete(arena: Arena) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle("Delete Arena")
        alertDialog.setMessage("Are you sure you want to delete this arena ? ")
        alertDialog.setPositiveButton(
                "Delete"
        ) { _, _ ->
            viewModel.deleteArena(arena)
        }
        alertDialog.setNegativeButton(
                "Cancel"
        ) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    private fun leave(arena: Arena) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle("Leave Arena")
        alertDialog.setMessage("Are you sure you want to leave this arena ? ")
        alertDialog.setPositiveButton(
                "Leave"
        ) { _, _ ->
            viewModel.leaveArena(arena)
        }
        alertDialog.setNegativeButton(
                "Cancel"
        ) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(context, "Sign In Successful", Toast.LENGTH_SHORT).show()
                        viewModel.authenticationCompleted()
                        viewModel.getArenas()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(context, "Login Failed: ", Toast.LENGTH_SHORT).show()
                    }
                }
    }
}