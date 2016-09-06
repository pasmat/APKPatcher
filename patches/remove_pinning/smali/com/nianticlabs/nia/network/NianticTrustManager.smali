.class public Lcom/nianticlabs/nia/network/NianticTrustManager;
.super Lcom/nianticlabs/nia/contextservice/ContextService;
.source "NianticTrustManager.java"

# interfaces
.implements Ljavax/net/ssl/X509TrustManager;


# direct methods
.method public constructor <init>(Landroid/content/Context;J)V
    .locals 0
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "nativeClassPointer"    # J

    .prologue
    .line 25
    invoke-direct {p0, p1, p2, p3}, Lcom/nianticlabs/nia/contextservice/ContextService;-><init>(Landroid/content/Context;J)V

    .line 26
    return-void
.end method

.method public static getTrustManager(Ljava/lang/String;Ljava/security/KeyStore;)Ljavax/net/ssl/X509TrustManager;
    .locals 6
    .param p0, "algorithm"    # Ljava/lang/String;
    .param p1, "keystore"    # Ljava/security/KeyStore;

    .prologue
    .line 57
    :try_start_0
    invoke-static {p0}, Ljavax/net/ssl/TrustManagerFactory;->getInstance(Ljava/lang/String;)Ljavax/net/ssl/TrustManagerFactory;

    move-result-object v0

    .line 58
    .local v0, "factory":Ljavax/net/ssl/TrustManagerFactory;
    invoke-virtual {v0, p1}, Ljavax/net/ssl/TrustManagerFactory;->init(Ljava/security/KeyStore;)V

    .line 60
    invoke-virtual {v0}, Ljavax/net/ssl/TrustManagerFactory;->getTrustManagers()[Ljavax/net/ssl/TrustManager;

    move-result-object v3

    array-length v4, v3

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v4, :cond_1

    aget-object v1, v3, v2

    .line 61
    .local v1, "tm":Ljavax/net/ssl/TrustManager;
    if-eqz v1, :cond_0

    instance-of v5, v1, Ljavax/net/ssl/X509TrustManager;

    if-eqz v5, :cond_0

    .line 62
    check-cast v1, Ljavax/net/ssl/X509TrustManager;
    :try_end_0
    .catch Ljava/security/NoSuchAlgorithmException; {:try_start_0 .. :try_end_0} :catch_1
    .catch Ljava/security/KeyStoreException; {:try_start_0 .. :try_end_0} :catch_0

    .line 69
    .end local v0    # "factory":Ljavax/net/ssl/TrustManagerFactory;
    .end local v1    # "tm":Ljavax/net/ssl/TrustManager;
    :goto_1
    return-object v1

    .line 60
    .restart local v0    # "factory":Ljavax/net/ssl/TrustManagerFactory;
    .restart local v1    # "tm":Ljavax/net/ssl/TrustManager;
    :cond_0
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 66
    .end local v0    # "factory":Ljavax/net/ssl/TrustManagerFactory;
    .end local v1    # "tm":Ljavax/net/ssl/TrustManager;
    :catch_0
    move-exception v2

    .line 69
    :cond_1
    :goto_2
    const/4 v1, 0x0

    goto :goto_1

    .line 65
    :catch_1
    move-exception v2

    goto :goto_2
.end method

.method public static getTrustManager(Ljava/security/KeyStore;)Ljavax/net/ssl/X509TrustManager;
    .locals 1
    .param p0, "keystore"    # Ljava/security/KeyStore;

    .prologue
    .line 50
    invoke-static {}, Ljavax/net/ssl/TrustManagerFactory;->getDefaultAlgorithm()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0, p0}, Lcom/nianticlabs/nia/network/NianticTrustManager;->getTrustManager(Ljava/lang/String;Ljava/security/KeyStore;)Ljavax/net/ssl/X509TrustManager;

    move-result-object v0

    return-object v0
.end method

.method private native nativeCheckClientTrusted([Ljava/security/cert/X509Certificate;Ljava/lang/String;)V
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/security/cert/CertificateException;
        }
    .end annotation
.end method

.method private native nativeCheckServerTrusted([Ljava/security/cert/X509Certificate;Ljava/lang/String;)V
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/security/cert/CertificateException;
        }
    .end annotation
.end method

.method private native nativeGetAcceptedIssuers()[Ljava/security/cert/X509Certificate;
.end method


# virtual methods
.method public checkClientTrusted([Ljava/security/cert/X509Certificate;Ljava/lang/String;)V
    .locals 2
    .param p1, "chain"    # [Ljava/security/cert/X509Certificate;
    .param p2, "authType"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/security/cert/CertificateException;
        }
    .end annotation

    .prologue
	return-void
.end method

.method public checkServerTrusted([Ljava/security/cert/X509Certificate;Ljava/lang/String;)V
    .locals 2
    .param p1, "chain"    # [Ljava/security/cert/X509Certificate;
    .param p2, "authType"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/security/cert/CertificateException;
        }
    .end annotation

    .prologue
	return-void
.end method

.method public getAcceptedIssuers()[Ljava/security/cert/X509Certificate;
    .locals 2

    .prologue
    .line 44
    const/4 v0, 0x0

    return-object v0
.end method
