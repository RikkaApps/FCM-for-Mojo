package moe.shizuku.fcmformojo.model;


import android.support.annotation.Keep;

@Keep
public class Password {

    private String raw;
    private String md5;

    public Password(String raw, String md5) {
        this.raw = raw;
        this.md5 = md5;
    }

    public String getRaw() {
        return raw;
    }

    public String getMd5() {
        return md5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Password password = (Password) o;

        if (raw != null ? !raw.equals(password.raw) : password.raw != null) return false;
        return md5 != null ? md5.equals(password.md5) : password.md5 == null;
    }

    @Override
    public int hashCode() {
        int result = raw != null ? raw.hashCode() : 0;
        result = 31 * result + (md5 != null ? md5.hashCode() : 0);
        return result;
    }
}
