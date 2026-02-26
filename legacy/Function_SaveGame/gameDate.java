package Function_SaveGame;

public class gameDate {
    public int day;
    public int month;
    public int year;

    private static final int DAYS_IN_MONTH = 30;

    public gameDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }
    public void advanceDay() {
        this.day++; // เพิ่มวันทีละ 1
        
        // ถ้าวันที่เกิน 30 ให้ขึ้นเดือนใหม่
        if (this.day > DAYS_IN_MONTH) {
            this.day = 1;
            this.month++;
            
            // ถ้าเดือนเกิน 12 ให้ขึ้นปีใหม่
            if (this.month > 12) {
                this.month = 1;
                this.year++;
            }
        }
    }
    @Override
    public String toString() {
        return day + "/" + month + "/" + year;
    }
}


