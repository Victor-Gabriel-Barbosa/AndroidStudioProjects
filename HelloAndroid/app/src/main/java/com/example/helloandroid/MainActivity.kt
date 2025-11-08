package com.example.helloandroid

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    var usuarioId: String? = null

    private val personagensMap = mapOf(
        "Frieren" to R.drawable.frieren,
        "Fern" to R.drawable.fern,
        "Stark" to R.drawable.stark,
        "Rimuru" to R.drawable.rimuru,
        "Benimaru" to R.drawable.benimaru
    )

    private var personagem: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textNome = findViewById<TextView>(R.id.textNome)
        val textSummon = findViewById<TextView>(R.id.textSummon)
        val imgSummon = findViewById<ImageView>(R.id.imgSummon)
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { res ->
                if (!res.isEmpty) {
                    val doc = res.documents.first()
                    usuarioId = doc.id
                    textNome.text = "Olá ${doc["nome"]} ${doc["sobrenome"]}, bem vindo de volta!"
                    personagem = doc["personagem"].toString()
                    textSummon.text = "Personagem sumonado anteriormente: ${personagem}"
                    imgSummon.setImageResource(personagensMap[personagem] ?: R.drawable.summon)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar usuário: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun invocar(view : View) {
        val textSummon = findViewById<TextView>(R.id.textSummon)

        val (nomePersonagem, imgId) = personagensMap.entries.random()
        personagem = nomePersonagem

        textSummon.text = "Personagem sumonado: ${personagem}"

        val imageSummon = findViewById<ImageView>(R.id.imgSummon)
        imageSummon.setImageResource(imgId)

        if (usuarioId != null) {
            db.collection("usuarios")
                .document(usuarioId ?: "")
                .update("personagem", personagem)
        } else {
            Toast.makeText(this, "Cadastre-se para salvar o personagem!", Toast.LENGTH_SHORT).show()
        }
    }

    fun cadastrar(view : View) {
        val editNome = findViewById<EditText>(R.id.editNome)
        val editSobrenome = findViewById<EditText>(R.id.editSobrenome)
        val textNome = findViewById<TextView>(R.id.textNome)

        val nome = editNome.text.trim()
        if (nome.isEmpty()) {
            editNome.error = "Por favor, digite um nome"
            return
        }

        val sobrenome = editSobrenome.text.trim()
        if (sobrenome.isEmpty()) {
            editSobrenome.error = "Por favor, digite um sobrenome"
            return
        }

        val usuario = hashMapOf(
            "nome" to nome.toString(),
            "sobrenome" to sobrenome.toString(),
            "personagem" to personagem
        )

        if (usuarioId == null) {
            db.collection("usuarios")
                .add(usuario)
                .addOnSuccessListener { res ->
                    usuarioId = res.id
                    textNome.text = "Olá $nome $sobrenome, seja bem vindo!"
                    editNome.text.clear()
                    editSobrenome.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao cadastrar usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("usuarios")
                .document(usuarioId!!)
                .set(usuario)
                .addOnSuccessListener { _ ->
                    textNome.text = "Olá $nome $sobrenome, seja bem vindo!"
                    editNome.text.clear()
                    editSobrenome.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao cadastrar usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun recarregar(view: View) {
        recreate()
    }
}