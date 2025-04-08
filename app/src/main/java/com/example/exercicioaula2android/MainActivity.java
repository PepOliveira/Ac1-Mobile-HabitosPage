package com.example.exercicioaula2android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    EditText edtHabito;
    Spinner spinnerCategoria;
    RadioGroup radioGroupFrequencia;
    RadioButton radioDiario, radioSemanal, radioMensal;
    Button btnSalvar;
    ListView listaHabitos;
    BancoHelper dbHelper;

    ArrayAdapter<String> listAdapter;
    ArrayList<String> habitos;
    HashMap<Integer, Integer> mapFeitoHoje;

    int habitoEditandoId = -1;
    long ultimoClique = 0;
    int ultimoItemClicado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtHabito = findViewById(R.id.editTextNome);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        radioGroupFrequencia = findViewById(R.id.radioGroupFrequencia);
        radioDiario = findViewById(R.id.radioDiario);
        radioSemanal = findViewById(R.id.radioSemanal);
        radioMensal = findViewById(R.id.radioMensal);
        btnSalvar = findViewById(R.id.btnSalvar);
        listaHabitos = findViewById(R.id.listViewHabitos);

        dbHelper = new BancoHelper(this);
        resetarFeitosSeNovoDia();

        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Exercício", "Leitura", "Sono", "Outros"});
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        btnSalvar.setOnClickListener(v -> {
            String nome = edtHabito.getText().toString().trim();
            String categoria = spinnerCategoria.getSelectedItem().toString();
            String frequencia = radioDiario.isChecked() ? "Diário" :
                    radioSemanal.isChecked() ? "Semanal" : "Mensal";

            if (nome.isEmpty()) {
                Toast.makeText(this, "Preencha o nome do hábito", Toast.LENGTH_SHORT).show();
                return;
            }

            long resultado;
            if (habitoEditandoId == -1) {
                resultado = dbHelper.inserirUsuario(nome, categoria, frequencia);
            } else {
                resultado = dbHelper.atualizarUsuario(habitoEditandoId, nome, categoria, frequencia);
                habitoEditandoId = -1;
            }

            if (resultado != -1) {
                Toast.makeText(this, "Hábito salvo com sucesso!", Toast.LENGTH_SHORT).show();
                edtHabito.setText("");
                radioGroupFrequencia.clearCheck();
                listarHabitos();
            } else {
                Toast.makeText(this, "Erro ao salvar hábito", Toast.LENGTH_SHORT).show();
            }
        });

        listaHabitos.setOnItemClickListener((parent, view, position, id) -> {
            long agora = System.currentTimeMillis();
            if (position == ultimoItemClicado && agora - ultimoClique < 600) {
                Cursor cursor = dbHelper.listarUsuarios();
                if (cursor.moveToPosition(position)) {
                    int habitoId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    dbHelper.marcarComoFeito(habitoId);
                    Toast.makeText(this, "Hábito marcado como feito!", Toast.LENGTH_SHORT).show();
                    listarHabitos();
                }
                cursor.close();
            } else {
                Cursor cursor = dbHelper.listarUsuarios();
                if (cursor.moveToPosition(position)) {
                    habitoEditandoId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                    String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                    String frequencia = cursor.getString(cursor.getColumnIndexOrThrow("frequencia"));

                    edtHabito.setText(nome);
                    spinnerCategoria.setSelection(categoriaAdapter.getPosition(categoria));
                    if (frequencia.equals("Diário")) radioDiario.setChecked(true);
                    else if (frequencia.equals("Semanal")) radioSemanal.setChecked(true);
                    else radioMensal.setChecked(true);
                }
                cursor.close();
            }
            ultimoClique = agora;
            ultimoItemClicado = position;
        });

        listaHabitos.setOnItemLongClickListener((parent, view, position, id) -> {
            Cursor cursor = dbHelper.listarUsuarios();
            if (cursor.moveToPosition(position)) {
                int habitoId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                dbHelper.deletarUsuario(habitoId);
                Toast.makeText(this, "Hábito excluído", Toast.LENGTH_SHORT).show();
                listarHabitos();
            }
            cursor.close();
            return true;
        });

        listarHabitos();
    }

    private void listarHabitos() {
        habitos = new ArrayList<>();
        mapFeitoHoje = new HashMap<>();

        Cursor cursor = dbHelper.listarUsuarios();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
            String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
            String frequencia = cursor.getString(cursor.getColumnIndexOrThrow("frequencia"));
            int feitoHoje = cursor.getInt(cursor.getColumnIndexOrThrow("feitoHoje"));

            String texto = nome + " - " + categoria + " - " + frequencia;
            habitos.add(texto);
            mapFeitoHoje.put(habitos.size() - 1, feitoHoje);
        }
        cursor.close();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, habitos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                int feito = mapFeitoHoje.containsKey(position) ? mapFeitoHoje.get(position) : 0;
                if (feito == 1) {
                    view.setBackgroundColor(getResources().getColor(R.color.feito));
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }

                return view;
            }
        };

        listaHabitos.setAdapter(listAdapter);
    }

    private void resetarFeitosSeNovoDia() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        String ultimoDia = prefs.getString("data_ultimo_acesso", "");
        String hoje = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        if (!hoje.equals(ultimoDia)) {
            dbHelper.resetarFeitoHoje();
            prefs.edit().putString("data_ultimo_acesso", hoje).apply();
        }
    }
}
