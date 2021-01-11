from flask import Flask
from flask import request

app = Flask(__name__)

@app.route('/save', methods=['POST'])
def save():
    set_num = request.args.get('setNumForLearning')
    cell_num = request.args.get('cellNum')
    b1_rssi = request.args.get('b1Rssi')
    b2_rssi = request.args.get('b2Rssi')
    b3_rssi = request.args.get('b3Rssi')

    if int(request.args.get('modelType')) == 5:
        model_type = "in"
    else:
        model_type = "out"

    if int(request.args.get('mode')) == 7:
        mode = "train"
    else:
        mode = "test"

    f = open("/home/ubuntu/smart_parking/my_code/project/{0}_{1}_{2}.csv".format(model_type, mode, set_num), 'a', encoding='utf-8')
    f.write("{0},{1},{2}".format(b1_rssi, b2_rssi, b3_rssi))
    for i in range(1, 16):
        f.write(",")
        if i == int(cell_num):
            f.write("1")
        else:
            f.write("0")
    f.write("\n")
    f.close()
    return " [b1Rssi: %s, b2Rssi: %s, b3Rssi: %s]" % (b1_rssi, b2_rssi, b3_rssi)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port="00")
