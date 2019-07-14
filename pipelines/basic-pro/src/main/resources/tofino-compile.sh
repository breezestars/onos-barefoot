#!/usr/bin/env bash

MAVERICKS_CPU_PORT=320
MONTARA_CPU_PORT=192

set -e

function usage() {
    echo "Please use -p for p4 source directory, -n for profile name, -f for other flags, -h for help"
}

while getopts "p:n:f:h?" argv; do
    case $argv in
        p)
            P4_SRC_DIR=$OPTARG
            ;;
        n)
            PROFILE=$OPTARG
            ;;
        f)
            OTHER_PP_FLAGS=$OPTARG
            ;;
        h)
            usage
            exit
            ;;
        ?)
            usage
            exit
            ;;
    esac
done

if [ -z "$P4_SRC_DIR" ]; then
    echo "Error: P4_SRC_DIR is not set"
    exit 1
fi

if [ ! -d "${P4_SRC_DIR}" ]; then
  echo "Error: unable to locate "$PROFILE" P4 sources at ${P4_SRC_DIR}"
  exit 1
fi

OUT_DIR=${P4_SRC_DIR}/p4c-out/${PROFILE}/tofino

function do_p4c {
    echo "*** Compiling profile '${PROFILE}' for $1 platform..."
    echo "*** Output in ${OUT_DIR}/$1"
    pp_flags="-DCPU_PORT=$2"
    mkdir -p ${OUT_DIR}/$1
    (set -x; bf-p4c --arch v1model -g \
        -o ${OUT_DIR}/$1 -I ${P4_SRC_DIR} \
        ${pp_flags} ${OTHER_PP_FLAGS} \
        --p4runtime-files ${OUT_DIR}/$1/p4info.txt,${OUT_DIR}/$1/p4info.json \
        fabric.p4)
    (set -x; mv ${OUT_DIR}/$1/pipe/* ${OUT_DIR}/$1/ \
            && rm -rf ${OUT_DIR}/$1/pipe)
    echo $2 > ${OUT_DIR}/$1/cpu_port.txt
    echo
}

do_p4c "montara" ${MONTARA_CPU_PORT}

