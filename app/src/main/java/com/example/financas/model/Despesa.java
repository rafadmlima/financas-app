package com.example.financas.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Despesa {
    private String titulo;
    private double valor;
    private String observacao;
    private Date dataCadastro;
    private String documentId;
    private String userId; // New field

    public Despesa() {
        // Construtor vazio necessário para o Firestore
    }

    public Despesa(String titulo, double valor, String observacao) {
        this.titulo = titulo;
        this.valor = valor;
        this.observacao = observacao;
    }

    // New constructor including userId
    public Despesa(String titulo, double valor, String observacao, String userId) {
        this.titulo = titulo;
        this.valor = valor;
        this.observacao = observacao;
        this.userId = userId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Getter and Setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
